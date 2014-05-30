/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.web.jsonrpc;

import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.rt.event.EventInstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author aploese
 */
public class JsonEventInstance {

    private int id;
    private int alarmLevel;
    private boolean active;
    private String activeTimestamp;
    private String rtnTimestamp;
    private String message;
    private boolean rtnApplicable;
    private String rtnMessage;
    private boolean acknowledged;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the alarmLevel
     */
    public int getAlarmLevel() {
        return alarmLevel;
    }

    /**
     * @param alarmLevel the alarmLevel to set
     */
    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    /**
     * @return the activeTimestamp
     */
    public String getActiveTimestamp() {
        return activeTimestamp;
    }

    /**
     * @param activeTimestamp the activeTimestamp to set
     */
    public void setActiveTimestamp(String activeTimestamp) {
        this.activeTimestamp = activeTimestamp;
    }

    /**
     * @return the rtnTimestamp
     */
    public String getRtnTimestamp() {
        return rtnTimestamp;
    }

    /**
     * @param rtnTimestamp the rtnTimestamp to set
     */
    public void setRtnTimestamp(String rtnTimestamp) {
        this.rtnTimestamp = rtnTimestamp;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the rtnApplicable
     */
    public boolean isRtnApplicable() {
        return rtnApplicable;
    }

    /**
     * @param rtnApplicable the rtnApplicable to set
     */
    public void setRtnApplicable(boolean rtnApplicable) {
        this.rtnApplicable = rtnApplicable;
    }

    /**
     * @return the rtnMessage
     */
    public String getRtnMessage() {
        return rtnMessage;
    }

    /**
     * @param rtnMessage the rtnMessage to set
     */
    public void setRtnMessage(String rtnMessage) {
        this.rtnMessage = rtnMessage;
    }

    /**
     * @return the acknowledged
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * @param acknowledged the acknowledged to set
     */
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public static JsonEventInstance wrap(EventInstance eventInstance, Localizer localizer) {
        final JsonEventInstance result = new JsonEventInstance();
        result.setId(eventInstance.getId());
        result.setActive(eventInstance.isActive());
        result.setAlarmLevel(eventInstance.getAlarmLevel());
        result.setActiveTimestamp(localizer.localizeTimeStamp(eventInstance.getActiveTimestamp(), true));
        result.setRtnApplicable(eventInstance.isRtnApplicable());
        if (eventInstance.getRtnMessage() != null) {
            result.setRtnMessage(localizer.localizeMessage(eventInstance.getRtnMessage()));
        }
        if (eventInstance.getRtnTimestamp() > 0) {
            result.setRtnTimestamp(localizer.localizeTimeStamp(eventInstance.getRtnTimestamp(), true));
        }
        result.setAcknowledged(eventInstance.isAcknowledged());
        result.setMessage(localizer.localizeMessage(eventInstance.getMessage()));
        return result;
    }

    public static Collection<JsonEventInstance> wrap(Collection<EventInstance> eventInstances, Localizer localizer) {
        List<JsonEventInstance> result = new ArrayList<>(eventInstances.size());
        for (EventInstance ei : eventInstances) {
            result.add(JsonEventInstance.wrap(ei, localizer));
        }
        return result;
    }

}
