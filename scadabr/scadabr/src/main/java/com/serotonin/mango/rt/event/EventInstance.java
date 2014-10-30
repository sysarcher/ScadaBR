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
package com.serotonin.mango.rt.event;

import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.vo.event.AlarmLevel;
import br.org.scadabr.vo.event.EventStatus;
import java.util.List;
import java.util.Map;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.handlers.EventHandlerRT;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.UserComment;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;

public class EventInstance {

    public interface RtnCauses {

        int RETURN_TO_NORMAL = 1;
        int SOURCE_DISABLED = 4;
    }

    public interface AlternateAcknowledgementSources {

        int DELETED_USER = 1;
        int MAINTENANCE_MODE = 2;
    }

    /**
     * Configuration field. Assigned by the database.
     */
    private int id = Common.NEW_ID;

    /**
     * Configuration field. Provided by the event producer. Identifies where the
     * event came from and what it means.
     */
    private final EventType eventType;

    /**
     * State field. The time that the event became active (i.e. was raised).
     */
    private final long activeTimestamp;

    /**
     * Configuration field. Is this type of event capable of returning to normal
     * (true), or is it stateless (false).
     */
    private final boolean rtnApplicable;

    /**
     * State field. The time that the event returned to normal.
     */
    private long rtnTimestamp;

    /**
     * State field. The action that caused the event to RTN. One of
     * {@link RtnCauses}
     */
    private int rtnCause;

    /**
     * Configuration field. The alarm level assigned to the event.
     *
     * @see AlarmLevels
     */
    private final AlarmLevel alarmLevel;

    /**
     * Configuration field. The message associated with the event.
     */
    private final LocalizableMessage message;

    /**
     * User comments on the event. Added in the events interface after the event
     * has been raised.
     */
    private List<UserComment> eventComments;

    private List<EventHandlerRT> handlers;

    private long acknowledgedTimestamp;
    private int acknowledgedByUserId;
    private String acknowledgedByUsername;
    private int alternateAckSource;

    //
    //
    // These fields are used only in the context of access by a particular user, providing state filled in from
    // the userEvents table.
    private boolean userNotified;
    private boolean silenced;

    //
    // Contextual data from the source that raised the event.
    private final Map<String, Object> context;

    public EventInstance(EventType eventType, long activeTimestamp, boolean rtnApplicable, AlarmLevel alarmLevel,
            LocalizableMessage message, Map<String, Object> context) {
        this.eventType = eventType;
        this.activeTimestamp = activeTimestamp;
        this.rtnApplicable = rtnApplicable;
        this.alarmLevel = alarmLevel;
        if (message == null) {
            this.message = new LocalizableMessageImpl("common.noMessage");
        } else {
            this.message = message;
        }
        this.context = context;
    }

    public LocalizableMessage getRtnMessage() {
        if (isActive()) {
            if (rtnCause == RtnCauses.RETURN_TO_NORMAL) {
                return new LocalizableMessageImpl("event.rtn.rtn");
            } else if (rtnCause == RtnCauses.SOURCE_DISABLED) {
                switch (eventType.getEventSource()) {
                    case DATA_POINT:
                        return new LocalizableMessageImpl("event.rtn.pointDisabled");
                    case DATA_SOURCE:
                        return new LocalizableMessageImpl("event.rtn.dsDisabled");
                    case PUBLISHER:
                        return new LocalizableMessageImpl("event.rtn.pubDisabled");
                    case MAINTENANCE:
                        return new LocalizableMessageImpl("event.rtn.maintDisabled");
                    default:
                        return new LocalizableMessageImpl("event.rtn.shutdown");
                }
            } else {
                return new LocalizableMessageImpl("event.rtn.unknown");
            }
        } else {
            return null;
        }
    }

    public LocalizableMessage getAckMessage() {
        if (isAcknowledged()) {
            if (acknowledgedByUserId != 0) {
                return new LocalizableMessageImpl("events.ackedByUser", acknowledgedByUsername);
            }
            if (alternateAckSource == AlternateAcknowledgementSources.DELETED_USER) {
                return new LocalizableMessageImpl("events.ackedByDeletedUser");
            }
            if (alternateAckSource == AlternateAcknowledgementSources.MAINTENANCE_MODE) {
                return new LocalizableMessageImpl("events.ackedByMaintenance");
            }
        }

        return null;
    }

    public LocalizableMessage getExportAckMessage() {
        if (isAcknowledged()) {
            if (acknowledgedByUserId != 0) {
                return new LocalizableMessageImpl("events.export.ackedByUser", acknowledgedByUsername);
            }
            if (alternateAckSource == AlternateAcknowledgementSources.DELETED_USER) {
                return new LocalizableMessageImpl("events.export.ackedByDeletedUser");
            }
            if (alternateAckSource == AlternateAcknowledgementSources.MAINTENANCE_MODE) {
                return new LocalizableMessageImpl("events.export.ackedByMaintenance");
            }
        }

        return null;
    }

    public boolean isAlarm() {
        return alarmLevel != AlarmLevel.NONE;
    }

    /**
     * This method should only be used by the EventDao for creating and
     * updating.
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    public EventStatus getEventState() {
        if (isActive()) {
            return EventStatus.ACTIVE;
        } else if (isRtnApplicable()) {
            return EventStatus.RTN;
        } else {
            return EventStatus.NORTN;
        }
    }

    public boolean isActive() {
        return rtnApplicable && rtnTimestamp == 0;
    }

    public void returnToNormal(long time, int rtnCause) {
        if (isActive()) {
            rtnTimestamp = time;
            this.rtnCause = rtnCause;
        }
    }

    public boolean isAcknowledged() {
        return acknowledgedTimestamp > 0;
    }

    public long getActiveTimestamp() {
        return activeTimestamp;
    }

    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventSources getEventSource() {
        return eventType.getEventSource();
    }
    
    public int getId() {
        return id;
    }

    public long getRtnTimestamp() {
        return rtnTimestamp;
    }

    public LocalizableMessage getMessage() {
        return message;
    }

    public boolean isRtnApplicable() {
        return rtnApplicable;
    }

    public void addEventComment(UserComment comment) {
        eventComments.add(comment);
    }

    public void setEventComments(List<UserComment> eventComments) {
        this.eventComments = eventComments;
    }

    public List<UserComment> getEventComments() {
        return eventComments;
    }

    public int getRtnCause() {
        return rtnCause;
    }

    public List<EventHandlerRT> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<EventHandlerRT> handlers) {
        this.handlers = handlers;
    }

    public boolean isUserNotified() {
        return userNotified;
    }

    public void setUserNotified(boolean userNotified) {
        this.userNotified = userNotified;
    }

    public boolean isSilenced() {
        return silenced;
    }

    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    public long getAcknowledgedTimestamp() {
        return acknowledgedTimestamp;
    }

    public void setAcknowledgedTimestamp(long acknowledgedTimestamp) {
        this.acknowledgedTimestamp = acknowledgedTimestamp;
    }

    public int getAcknowledgedByUserId() {
        return acknowledgedByUserId;
    }

    public void setAcknowledgedByUserId(int acknowledgedByUserId) {
        this.acknowledgedByUserId = acknowledgedByUserId;
    }

    public String getAcknowledgedByUsername() {
        return acknowledgedByUsername;
    }

    public void setAcknowledgedByUsername(String acknowledgedByUsername) {
        this.acknowledgedByUsername = acknowledgedByUsername;
    }

    public int getAlternateAckSource() {
        return alternateAckSource;
    }

    public void setAlternateAckSource(int alternateAckSource) {
        this.alternateAckSource = alternateAckSource;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
