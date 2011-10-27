/*
Mango - Open Source M2M - http://mango.serotoninsoftware.com
Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
@author Matthew Lohbihler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.rt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.handlers.EmailHandlerRT;
import com.serotonin.mango.rt.event.handlers.EventHandlerRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.DataPointEventType;
import com.serotonin.mango.rt.event.type.DataSourceEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.EventHandlerVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.ILifecycle;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@Service
public class EventManager implements ILifecycle {

    @Autowired
    private Common common;
//TODO LookuP???    @Autowired
    private EmailHandlerRT emailHandlerRT;
    @Autowired
    private Permissions permissions;
    @Autowired
    private EventDao eventDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RuntimeManager runtimeManger;
    private final static Logger LOG = LoggerFactory.getLogger(EventManager.class);
    private final List<EventInstance> activeEvents = new CopyOnWriteArrayList();
    private long lastAlarmTimestamp = 0;
    private AlarmLevels highestActiveAlarmLevel = AlarmLevels.NONE;

    //
    //
    // Basic event management.
    //
    public void raiseEvent(EventType type, long time, boolean rtnApplicable, AlarmLevels alarmLevel,
            LocalizableMessage message, Map<String, Object> context) {
        // Check if there is an event for this type already active.
        EventInstance dup = get(type);
        if (dup != null) {
            // Check the duplicate handling.
            int dh = type.getDuplicateHandling();
            if (dh == EventType.DuplicateHandling.DO_NOT_ALLOW) {
                // Create a log error...
                LOG.error("An event was raised for a type that is already active: type=" + type + ", message="
                        + message.getKey());
                // ... but ultimately just ignore the thing.
                return;
            }

            if (dh == EventType.DuplicateHandling.IGNORE) // Safely return.
            {
                return;
            }

            if (dh == EventType.DuplicateHandling.IGNORE_SAME_MESSAGE) {
                // Ignore only if the message is the same. There may be events of this type with different messages,
                // so look through them all for a match.
                for (EventInstance e : getAll(type)) {
                    if (e.getMessage().equals(message)) {
                        return;
                    }
                }
            }

            // Otherwise we just continue...
        }

        // Determine if the event should be suppressed.
        boolean suppressed = isSuppressed(type);

        EventInstance evt = new EventInstance(type, time, rtnApplicable, alarmLevel, message, context);

        if (!suppressed) {
            setHandlers(evt);
        }

        // Get id from database by inserting event immediately.
        eventDao.saveEvent(evt);

        // Create user alarm records for all applicable users
        List<Integer> eventUserIds = new ArrayList();
        Set<String> emailUsers = new HashSet();

        for (User user : userDao.getActiveUsers()) {
            // Do not create an event for this user if the event type says the user should be skipped.
            if (type.excludeUser(user)) {
                continue;
            }

            if (permissions.hasEventTypePermission(user, type)) {
                eventUserIds.add(user.getId());
                if (evt.isAlarm() && user.getReceiveAlarmEmails() != AlarmLevels.NONE && alarmLevel.compareTo(user.getReceiveAlarmEmails()) >= 0) {
                    emailUsers.add(user.getEmail());
                }
            }
        }

        if (eventUserIds.size() > 0) {
            eventDao.insertUserEvents(evt.getId(), eventUserIds, evt.isAlarm());
            if (!suppressed && evt.isAlarm()) {
                lastAlarmTimestamp = System.currentTimeMillis();
            }
        }

        if (evt.isRtnApplicable()) {
            activeEvents.add(evt);
        }

        if (suppressed) {
            eventDao.ackEvent(evt.getId(), time, 0, EventInstance.AlternateAcknowledgementSources.MAINTENANCE_MODE);
        } else {
            if (evt.isRtnApplicable()) {
                if (alarmLevel.compareTo(highestActiveAlarmLevel) > 0) {
                    AlarmLevels oldValue = highestActiveAlarmLevel;
                    highestActiveAlarmLevel = alarmLevel;
                    raiseEvent(new SystemEventType(SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED), time,
                            false, getAlarmLevelChangeMessage("event.alarmMaxIncreased", oldValue));
                }
            }

            // Call raiseEvent handlers.
            handleRaiseEvent(evt, emailUsers);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Event raised: type=" + type + ", message=" + message.getLocalizedMessage(common.getBundle()));
            }
        }
    }

    public void returnToNormal(EventType type, long time) {
        returnToNormal(type, time, EventInstance.RtnCauses.RETURN_TO_NORMAL);
    }

    public void returnToNormal(EventType type, long time, int cause) {
        EventInstance evt = remove(type);

        // Loop in case of multiples
        while (evt != null) {
            resetHighestAlarmLevel(time, false);

            evt.returnToNormal(time, cause);
            eventDao.saveEvent(evt);

            // Call inactiveEvent handlers.
            handleInactiveEvent(evt);

            // Check for another
            evt = remove(type);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Event returned to normal: type=" + type);
        }
    }

    private void deactivateEvent(EventInstance evt, long time, int inactiveCause) {
        activeEvents.remove(evt);
        resetHighestAlarmLevel(time, false);
        evt.returnToNormal(time, inactiveCause);
        eventDao.saveEvent(evt);

        // Call inactiveEvent handlers.
        handleInactiveEvent(evt);
    }

    public long getLastAlarmTimestamp() {
        return lastAlarmTimestamp;
    }

    //
    //
    // Canceling events.
    //
    public void cancelEventsForDataPoint(DataPointRT dataPointRt) {
        for (EventInstance e : activeEvents) {
            if (e.getEventType().getDataPointId() == dataPointRt.getId()) {
                deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
            }
        }
    }

    @Deprecated //TODO use RT or VO
    public void cancelEventsForDataPoint(DataPointVO dataPointVo) {
        for (EventInstance e : activeEvents) {
            if (e.getEventType().getDataPointId() == dataPointVo.getId()) {
                deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
            }
        }
    }

    public void cancelEventsForDataSource(DataSourceVO<?> dataSource) {
        for (EventInstance e : activeEvents) {
            if (e.getEventType().getDataSourceId() == dataSource.getId()) {
                deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
            }
        }
    }

    public void cancelEventsForPublisher(int publisherId) {
        for (EventInstance e : activeEvents) {
            if (e.getEventType().getPublisherId() == publisherId) {
                deactivateEvent(e, System.currentTimeMillis(), EventInstance.RtnCauses.SOURCE_DISABLED);
            }
        }
    }

    private void resetHighestAlarmLevel(long time, boolean init) {
        AlarmLevels max = AlarmLevels.NONE;
        for (EventInstance e : activeEvents) {
            if (e.getAlarmLevel().compareTo(max) > 0) {
                max = e.getAlarmLevel();
            }
        }

        if (!init) {
            if (max.compareTo(highestActiveAlarmLevel) > 0) {
                AlarmLevels oldValue = highestActiveAlarmLevel;
                highestActiveAlarmLevel = max;
                raiseEvent(new SystemEventType(SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED), time,
                        false, getAlarmLevelChangeMessage("event.alarmMaxIncreased", oldValue));
            } else if (max.compareTo(highestActiveAlarmLevel) < 0) {
                AlarmLevels oldValue = highestActiveAlarmLevel;
                highestActiveAlarmLevel = max;
                raiseEvent(new SystemEventType(SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED), time,
                        false, getAlarmLevelChangeMessage("event.alarmMaxDecreased", oldValue));
            }
        }
    }

    private LocalizableMessage getAlarmLevelChangeMessage(String key, AlarmLevels oldValue) {
        return new LocalizableMessage(key, oldValue.getMessageI18n(),
                highestActiveAlarmLevel.getMessageI18n());
    }

    //
    //
    // Lifecycle interface
    //
    @Override
    @PostConstruct
    public void initialize() {
        // Get all active events from the database.
        activeEvents.addAll(eventDao.getActiveEvents());
        lastAlarmTimestamp = System.currentTimeMillis();
        resetHighestAlarmLevel(lastAlarmTimestamp, true);
    }

    @Override
    @PreDestroy
    public void terminate() {
        // no op
    }

    @Override
    @PreDestroy
    public void joinTermination() {
        // no op
    }

    //
    //
    // Convenience
    //
    /**
     * Returns the first event instance with the given type, or null is there is none.
     */
    private EventInstance get(EventType type) {
        for (EventInstance e : activeEvents) {
            if (e.getEventType().equals(type)) {
                return e;
            }
        }
        return null;
    }

    private List<EventInstance> getAll(EventType type) {
        List<EventInstance> result = new ArrayList();
        for (EventInstance e : activeEvents) {
            if (e.getEventType().equals(type)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Finds and removes the first event instance with the given type. Returns null if there is none.
     *
     * @param type
     * @return
     */
    private EventInstance remove(EventType type) {
        for (EventInstance e : activeEvents) {
            if (e.getEventType().equals(type)) {
                activeEvents.remove(e);
                return e;
            }
        }
        return null;
    }

    private void setHandlers(EventInstance evt) {
        List<EventHandlerVO> vos = eventDao.getEventHandlers(evt.getEventType());
        List<EventHandlerRT> rts = null;
        for (EventHandlerVO vo : vos) {
            if (!vo.isDisabled()) {
                if (rts == null) {
                    rts = new ArrayList();
                }
                rts.add(vo.createRuntime());
            }
        }
        if (rts != null) {
            evt.setHandlers(rts);
        }
    }

    private void handleRaiseEvent(EventInstance evt, Set<String> defaultAddresses) {
        if (evt.getHandlers() != null) {
            for (EventHandlerRT h : evt.getHandlers()) {
                h.eventRaised(evt);

                // If this is an email handler, remove any addresses to which it was sent from the default addresses
                // so that the default users do not receive multiple notifications.
                if (h instanceof EmailHandlerRT) {
                    for (String addr : ((EmailHandlerRT) h).getActiveRecipients()) {
                        defaultAddresses.remove(addr);
                    }
                }
            }
        }

        if (!defaultAddresses.isEmpty()) {
            // If there are still any addresses left in the list, send them the notification.
            emailHandlerRT.sendActiveEmail(evt, defaultAddresses);
        }
    }

    private void handleInactiveEvent(EventInstance evt) {
        if (evt.getHandlers() != null) {
            for (EventHandlerRT h : evt.getHandlers()) {
                h.eventInactive(evt);
            }
        }
    }

    private boolean isSuppressed(EventType eventType) {
        if (eventType instanceof DataSourceEventType) // Data source events can be suppressed by maintenance events.
        {
            runtimeManger.isActiveMaintenanceEvent(eventType.getDataSourceId());
        }

        if (eventType instanceof DataPointEventType) // Data point events can be suppressed by maintenance events on their data sources.
        {
            runtimeManger.isActiveMaintenanceEvent(eventType.getDataSourceId());
        }

        return false;
    }

    public void raiseEvent(SystemEventType type, long time, boolean rtn, LocalizableMessage message) {
        EventTypeVO vo = type.getEventType(type.getSystemEventTypeId());
        AlarmLevels alarmLevel = vo.getAlarmLevel();
        raiseEvent(type, time, rtn, alarmLevel, message, null);
    }

    //Audit eventtypes !!!
    public void raiseAddedEvent(int auditEventTypeId, ChangeComparable<?> o) {
        List<LocalizableMessage> list = new ArrayList();
        o.addProperties(list);
        raiseEvent(auditEventTypeId, o, "event.audit.added", list.toArray());
    }

    public <T> void raiseChangedEvent(int auditEventTypeId, T from, ChangeComparable<T> to) {
        List<LocalizableMessage> changes = new ArrayList();
        to.addPropertyChanges(changes, from);
        if (changes.isEmpty()) // If the object wasn't in fact changed, don't raise an event.
        {
            return;
        }
        raiseEvent(auditEventTypeId, to, "event.audit.changed", changes.toArray());
    }

    public void raiseDeletedEvent(int auditEventTypeId, ChangeComparable<?> o) {
        List<LocalizableMessage> list = new ArrayList();
        o.addProperties(list);
        raiseEvent(auditEventTypeId, o, "event.audit.deleted", list.toArray());
    }

    private void raiseEvent(int auditEventTypeId, ChangeComparable<?> o, String key, Object[] props) {
        User user = common.getUser();
        Object username;
        if (user != null) {
            username = user.getUsername() + " (" + user.getId() + ")";
        } else {
            String descKey = common.getBackgroundProcessDescription();
            if (descKey == null) {
                username = new LocalizableMessage("common.unknown");
            } else {
                username = new LocalizableMessage(descKey);
            }
        }

        LocalizableMessage message = new LocalizableMessage(key, username, new LocalizableMessage(o.getTypeKey()),
                o.getId(), new LocalizableMessage("event.audit.propertyList." + props.length, props));

        AuditEventType type = new AuditEventType(auditEventTypeId, o.getId());
        type.setRaisingUser(user);

        raiseEvent(type, System.currentTimeMillis(), false,
                type.getEventType(type.getAuditEventTypeId()).getAlarmLevel(), message, null);
    }
}
