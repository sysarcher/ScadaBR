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

import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.MaintenanceEventDao;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.db.dao.ScheduledEventDao;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An event class specifies the type of event that was raised.
 *
 * @author Matthew Lohbihler
 */
@Configurable
abstract public class EventType {

    @Autowired
    private EventManager eventManager;
    
    //TODO do we need the context or can this data be retieved later???
    public void fire(Map<String, Object> context, String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, System.currentTimeMillis(), new LocalizableMessageImpl(i18nKey, i18nArgs), context);
    }

    //TODO do we need the context or can this data be retieved later???
    public void fire(Map<String, Object> context, long timestamp, LocalizableMessage msg) {
        eventManager.handleFiredEvent(this, timestamp, msg, context);
    }

    //TODO do we need the context or can this data be retieved later???
    public void fire(Map<String, Object> context, long timestamp, String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, timestamp, new LocalizableMessageImpl(i18nKey, i18nArgs), context);
    }


    public void fire(String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, System.currentTimeMillis(), new LocalizableMessageImpl(i18nKey, i18nArgs), null);
    }

    public void fire(long timestamp, String i18nKey, Object... i18nArgs) {
        eventManager.handleFiredEvent(this, timestamp, new LocalizableMessageImpl(i18nKey, i18nArgs), null);
    }

    public void fire(LocalizableMessage msg) {
        eventManager.handleFiredEvent(this, System.currentTimeMillis(), msg, null);
    }

    public void fire(long timestamp, LocalizableMessage msg) {
        eventManager.handleFiredEvent(this, timestamp, msg, null);
    }

    /**
     * Alarm is gone, so clear it
     *
     */
    public void clearAlarm() {
        eventManager.handleAlarmCleared(this, System.currentTimeMillis());
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
        eventManager.handleAlarmDisabled(this, System.currentTimeMillis());
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

    /**
     * Currently ony the Alarmlevel for firing the event or raising the alarm.
     * EventIntsance does not know the Alarmlevel.
     *
     * @return the alarmLevel
     */
    public abstract AlarmLevel getAlarmLevel();

    public abstract boolean isStateful();

}
