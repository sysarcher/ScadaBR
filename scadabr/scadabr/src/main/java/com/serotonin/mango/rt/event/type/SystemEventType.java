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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.event.EventTypeVO;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.event.type.SystemEventSource;
import java.util.EnumMap;

@JsonRemoteEntity
public class SystemEventType extends EventType {

    //
    // /
    // / Static stuff
    // /
    //
    private static final String SYSTEM_SETTINGS_PREFIX = "systemEventAlarmLevel";

    public static final Map<SystemEventSource, EventTypeVO> SYSTEM_EVENT_TYPES = new EnumMap<>(SystemEventSource.class);

    //TODO fix Alarmlevel from SystemSettingsDao for now only default 
    static {
        for (SystemEventSource s : SystemEventSource.values()) {
            SYSTEM_EVENT_TYPES.put(s, new EventTypeVO(EventSources.SYSTEM, s.mangoDbId, 0, s, s.defaultAlarmLevel));
        }

    }
    /*    
     private static void addEventTypeVO(AuditEventSource type, String key) {
     auditEventTypes.add(new EventTypeVO(EventSources.AUDIT, type, 0, new LocalizableMessageImpl(key),
     SystemSettingsDao.getAlarmLevel(AUDIT_SETTINGS_PREFIX + type, AlarmLevel.INFORMATION)));
     }
     */

    public static EventTypeVO getEventType(SystemEventSource type) {
        return SYSTEM_EVENT_TYPES.get(type);
    }

    public static void setEventTypeAlarmLevel(SystemEventSource type, AlarmLevel alarmLevel) {
        EventTypeVO et = getEventType(type);
        et.setAlarmLevel(alarmLevel);

        SystemSettingsDao dao = SystemSettingsDao.getInstance();
        dao.setAlarmLevel(SYSTEM_SETTINGS_PREFIX + type, alarmLevel);
    }

    @Deprecated // Use Eventmanager
    public static void raiseEvent(SystemEventType type, long time, boolean rtn, LocalizableMessage message) {
        EventTypeVO vo = getEventType(type.getSystemEventType());
        AlarmLevel alarmLevel = vo.getAlarmLevel();
        Common.ctx.getEventManager().raiseEvent(type, time, rtn, alarmLevel, message, null);
    }

    @Deprecated // Use Eventmanager
    public static void returnToNormal(SystemEventType type, long time) {
        Common.ctx.getEventManager().returnToNormal(type, time);
    }

    //
    // /
    // / Instance stuff
    // /
    //
    private SystemEventSource systemEventType;
    private int referenceId;
    private DuplicateHandling duplicateHandling = DuplicateHandling.ALLOW;

    public SystemEventType() {
        // Required for reflection.
    }

    public SystemEventType(SystemEventSource systemEventType) {
        this.systemEventType = systemEventType;
    }

    public SystemEventType(SystemEventSource systemEventType, int referenceId) {
        this(systemEventType);
        this.referenceId = referenceId;
    }

    public SystemEventType(SystemEventSource systemEventType, int referenceId, DuplicateHandling duplicateHandling) {
        this(systemEventType);
        this.referenceId = referenceId;
        this.duplicateHandling = duplicateHandling;
    }

    @Override
    public EventSources getEventSource() {
        return EventSources.SYSTEM;
    }

    public SystemEventSource getSystemEventType() {
        return systemEventType;
    }

    @Override
    public boolean isSystemMessage() {
        return true;
    }

    @Override
    public String toString() {
        return "SystemEventType(eventType=" + systemEventType + ")";
    }

    @Override
    public DuplicateHandling getDuplicateHandling() {
        return duplicateHandling;
    }

    public int getReferenceId() {
        return referenceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + referenceId;
        result = prime * result + systemEventType.mangoDbId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SystemEventType other = (SystemEventType) obj;
        if (referenceId != other.referenceId) {
            return false;
        }
        return systemEventType == other.systemEventType;
    }

    //
    // /
    // / Serialization
    // /
    //
    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("systemType", systemEventType.name());
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        systemEventType = SystemEventSource.valueOf(json.getString("systemType"));
    }
}
