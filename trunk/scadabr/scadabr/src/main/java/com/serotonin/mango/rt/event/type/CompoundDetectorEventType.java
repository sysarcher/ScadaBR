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
import br.org.scadabr.utils.ImplementMeException;
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.vo.event.CompoundEventDetectorVO;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class CompoundDetectorEventType extends EventType {

    private int compoundDetectorId;
    private DuplicateHandling duplicateHandling = DuplicateHandling.IGNORE;
    private AlarmLevel alarmLevel;
    private boolean stateful;

    public CompoundDetectorEventType() {
        // Required for reflection.
    }

    @Deprecated
    public CompoundDetectorEventType(int compoundDetectorId) {
        this.compoundDetectorId = compoundDetectorId;
/*        
        this.alarmLevel = vo.getAlarmLevel();
        if (!vo.isReturnToNormal()) {
            duplicateHandling = DuplicateHandling.ALLOW;
        }
        */
    }

    public CompoundDetectorEventType(CompoundEventDetectorVO vo) {
        this.compoundDetectorId = vo.getId();
        this.alarmLevel = vo.getAlarmLevel();
        this.stateful = vo.isReturnToNormal();
        if (!vo.isReturnToNormal()) {
            duplicateHandling = DuplicateHandling.ALLOW;
        }
    }

    @Override
    public EventSources getEventSource() {
        return EventSources.COMPOUND;
    }

    public int getCompoundDetectorId() {
        return compoundDetectorId;
    }

    @Override
    public String toString() {
        return "CompoundDetectorEventType(compoundDetectorId=" + compoundDetectorId + ")";
    }

    @Override
    public DuplicateHandling getDuplicateHandling() {
        return duplicateHandling;
    }

    public void setDuplicateHandling(DuplicateHandling duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    public int getReferenceId1() {
        return compoundDetectorId;
    }

    public int getReferenceId2() {
        return 0;
    }

    @Override
    public int getCompoundEventDetectorId() {
        return compoundDetectorId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + compoundDetectorId;
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
        CompoundDetectorEventType other = (CompoundDetectorEventType) obj;
        if (compoundDetectorId != other.compoundDetectorId) {
            return false;
        }
        return true;
    }

    //
    // /
    // / Serialization
    // /
    //
    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("XID", CompoundEventDetectorDao.getInstance().getCompoundEventDetector(compoundDetectorId).getXid());
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        compoundDetectorId = getCompoundEventDetectorId(json, "XID");
    }

    @Override
    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

    @Override
    public boolean isStateful() {
        return stateful;
    }

}
