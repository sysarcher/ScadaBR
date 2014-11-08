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
package com.serotonin.mango.rt.event.type;

import br.org.scadabr.ShouldNeverHappenException;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.json.JsonSerializable;
import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.MaintenanceEventDao;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.db.dao.ScheduledEventDao;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.CompoundEventDetectorVO;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import com.serotonin.mango.vo.event.ScheduledEventVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An event class specifies the type of event that was raised.
 *
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity(typeFactory = EventTypeFactory.class)
@Configurable
abstract public class EventType implements JsonSerializable {

    @Autowired
    private EventManager eventManager;
    @Autowired
    private DataPointDao dataPointDao;
    @Autowired
    private DataSourceDao dataSourceDao;

    //TODO do we need the context or can this data be retieved later???
    @Deprecated
    public void fire(Map<String, Object> context, String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, i18nKey, i18nArgs);
    }

    //TODO do we need the context or can this data be retieved later???
    @Deprecated
    public void fire(Map<String, Object> context, long timestamp, LocalizableMessage msg) {
        eventManager.handleFiredEvent(this, timestamp, msg);
    }


    public void fire(String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, i18nKey, i18nArgs);
    }

    public void fire(long timestamp, String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, timestamp, i18nKey, i18nArgs);
    }

    public void fire(LocalizableMessage msg) {
        eventManager.handleFiredEvent(this, msg);
    }

    public void fire(long timestamp, LocalizableMessage msg) {
        eventManager.handleFiredEvent(this, timestamp, msg);
    }

    //TODO do we need the context or can this data be retieved later???
    @Deprecated
    public void raiseAlarm(Map<String, Object> context, String i18nKey, Object... i18nArgs) {
        eventManager.handleRaisedAlarm(this, i18nKey, i18nArgs);
    }

    //TODO do we need the context or can this data be retieved later???
    @Deprecated
    public void raiseAlarm(Map<String, Object> context, long timestamp, String i18nKey, Object... i18nArgs) {
        eventManager.handleRaisedAlarm(this, timestamp, i18nKey, i18nArgs);
    }

    //TODO do we need the context or can this data be retieved later???
    @Deprecated
    public void raiseAlarm(Map<String, Object> context, long timestamp, LocalizableMessage msg) {
        eventManager.handleRaisedAlarm(this, timestamp, msg);
    }

    public void raiseAlarm(String i18nKey, Object... i18nArgs) {
        eventManager.handleRaisedAlarm(this, i18nKey, i18nArgs);
    }

    public void raiseAlarm(long timestamp, String i18nKey, Object... i18nArgs) {
        eventManager.handleRaisedAlarm(this, timestamp, i18nKey, i18nArgs);
    }

    public void raiseAlarm(LocalizableMessage msg) {
        eventManager.handleRaisedAlarm(this, msg);
    }

    public void raiseAlarm(long timestamp, LocalizableMessage msg) {
        eventManager.handleRaisedAlarm(this, timestamp, msg);
    }

    public void clearAlarm() {
        eventManager.handleAlarmCleared(this);
    }

    /**
     * Alarm is gone, so clear it
     *
     * @param timestamp
     */
    public void clearAlarm(long timestamp) {
        eventManager.handleAlarmCleared(this, timestamp);
    }

    public void disableAlarm() {
        eventManager.handleAlarmDisabled(this);
    }


    abstract public EventSources getEventSource();

    /**
     * Convenience method that keeps us from having to cast.
     *
     * @return false here, but the system message implementation will return
     * true.
     */
    public boolean isSystemMessage() {
        return false;
    }

    /**
     * Convenience method that keeps us from having to cast.
     *
     * @return
     * @throws ShouldNeverHappenException if accessed from wrong childInstance
     */
    public int getDataSourceId() throws ShouldNeverHappenException {
        throw new ShouldNeverHappenException("getDataSourceId() from" + this.getClass().getCanonicalName());
    }

    /**
     * Convenience method that keeps us from having to cast.
     *
     * @return
     * @throws ShouldNeverHappenException if accessed from wrong childInstance
     */
    public int getDataPointId() throws ShouldNeverHappenException {
        throw new ShouldNeverHappenException("getDataPointId() from" + this.getClass().getCanonicalName());
    }

    /**
     * Convenience method that keeps us from having to cast.
     *
     * @return
     * @throws ShouldNeverHappenException if accessed from wrong childInstance
     */
    public int getScheduleId() throws ShouldNeverHappenException {
        throw new ShouldNeverHappenException("getScheduleId() from" + this.getClass().getCanonicalName());
    }

    /**
     * Convenience method that keeps us from having to cast.
     *
     * @return
     * @throws ShouldNeverHappenException if accessed from wrong childInstance
     */
    public int getCompoundEventDetectorId() throws ShouldNeverHappenException {
        throw new ShouldNeverHappenException("getCompoundEventDetectorId() from" + this.getClass().getCanonicalName());
    }

    /**
     * Convenience method that keeps us from having to cast.
     *
     * @return
     * @throws ShouldNeverHappenException if accessed from wrong childInstance
     */
    public int getPublisherId() throws ShouldNeverHappenException {
        throw new ShouldNeverHappenException("getPublisherId() from" + this.getClass().getCanonicalName());
    }

    /**
     * Determines whether an event type that, once raised, will always first be
     * deactivated or whether overriding events can be raised. Overrides can
     * occur in data sources and point locators where a retry of a failed action
     * causes the same event type to be raised without the previous having
     * returned to normal.
     *
     * @return whether this event type can be overridden with newer event
     * instances.
     */
    abstract public DuplicateHandling getDuplicateHandling();

    /**
     * Determines if the notification of this event to the given user should be
     * suppressed. Useful if the action of the user resulted in the event being
     * raised.
     *
     * @return
     */
    public boolean excludeUser(@SuppressWarnings("unused") User user) {
        return false;
    }

    //
    // /
    // / Serialization
    // /
    //
    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("sourceType", getEventSource().name());
    }

    /**
     * @throws JsonException
     */
    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        // no op. See the factory
    }

    protected int getInt(JsonObject json, String name, ExportCodes codes) throws JsonException {
        String text = json.getString(name);
        if (text == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing", name, codes.getCodeList());
        }

        int i = codes.getId(text);
        if (i == -1) {
            throw new LocalizableJsonException("emport.error.eventType.invalid", name, text, codes.getCodeList());
        }

        return i;
    }

    // protected int getUserId(JsonObject json, String name) throws JsonException {
    // String username = json.getString(name);
    // if (username == null)
    // throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
    // User user = new UserDao().getUser(username);
    // if (user == null)
    // throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, username);
    // return user.getId();
    // }
    //
    protected int getCompoundEventDetectorId(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
        }
        CompoundEventDetectorVO ced = CompoundEventDetectorDao.getInstance().getCompoundEventDetector(xid);
        if (ced == null) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
        }
        return ced.getId();
    }

    // protected int getEventHandlerId(JsonObject json, String name) throws JsonException {
    // String xid = json.getString(name);
    // if (xid == null)
    // throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
    // EventHandlerVO eh = new EventDao().getEventHandler(xid);
    // if (eh == null)
    // throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
    // return eh.getId();
    // }
    //
    protected int getScheduledEventId(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
        }
        ScheduledEventVO se = ScheduledEventDao.getInstance().getScheduledEvent(xid);
        if (se == null) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
        }
        return se.getId();
    }

    protected int getDataPointId(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
        }
        DataPointVO dp = dataPointDao.getDataPoint(xid);
        if (dp == null) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
        }
        return dp.getId();
    }

    // protected int getPointLinkId(JsonObject json, String name) throws JsonException {
    // String xid = json.getString(name);
    // if (xid == null)
    // throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
    // PointLinkVO pl = new PointLinkDao().getPointLink(xid);
    // if (pl == null)
    // throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
    // return pl.getId();
    // }
    protected int getPointEventDetectorId(JsonObject json, String dpName, String pedName) throws JsonException {
        return getPointEventDetectorId(json, getDataPointId(json, dpName), pedName);
    }

    protected int getPointEventDetectorId(JsonObject json, int dpId, String pedName) throws JsonException {
        String pedXid = json.getString(pedName);
        if (pedXid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", pedName);
        }
        int id = dataPointDao.getDetectorId(pedXid, dpId);
        if (id == -1) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", pedName, pedXid);
        }

        return id;
    }

    protected DataSourceVO<?> getDataSource(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
        }
        DataSourceVO<?> ds = dataSourceDao.getDataSource(xid);
        if (ds == null) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
        }
        return ds;
    }

    protected PublisherVO<?> getPublisher(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
        }
        PublisherVO<?> pb = PublisherDao.getInstance().getPublisher(xid);
        if (pb == null) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
        }
        return pb;
    }

    protected int getMaintenanceEventId(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null) {
            throw new LocalizableJsonException("emport.error.eventType.missing.reference", name);
        }
        MaintenanceEventVO me = MaintenanceEventDao.getInstance().getMaintenanceEvent(xid);
        if (me == null) {
            throw new LocalizableJsonException("emport.error.eventType.invalid.reference", name, xid);
        }
        return me.getId();
    }

    /**
     * Currently ony the Alarmlevel for firing the event or raising the alarm.
     * EventIntsance does not know the Alarmlevel.
     *
     * @return the alarmLevel
     */
    public abstract AlarmLevel getAlarmLevel();

}
