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

import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.vo.event.AlarmLevel;
import br.org.scadabr.vo.event.type.SystemEventSource;
import com.serotonin.mango.rt.EventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@JsonRemoteEntity
public class SystemEventType extends EventType {

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
        result = prime * result + systemEventType.getId();
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

    @Override
    public AlarmLevel getAlarmLevel() {
        return systemEventType.getAlarmLevel();
    }
}
