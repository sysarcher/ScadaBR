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
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@JsonRemoteEntity
@Configurable
public class DataPointEventType extends EventType {
    @Autowired
    private DataPointDao dataPointDao;

    private int dataSourceId = -1;
    private int dataPointId;
    private int pointEventDetectorId;
    private DuplicateHandling duplicateHandling = DuplicateHandling.IGNORE;
    private AlarmLevel alarmLevel;
    
    public DataPointEventType() {
        // Required for reflection.
    }

    @Deprecated
    public DataPointEventType(int dataPointId, int pointEventDetectorId) {
        this.dataPointId = dataPointId;
        this.pointEventDetectorId = pointEventDetectorId;
/*
        this.alarmLevel = vo.getAlarmLevel();
        if (!vo.isRtnApplicable()) {
            duplicateHandling = DuplicateHandling.ALLOW;
        }
        */
    }

    public DataPointEventType(PointEventDetectorVO vo) {
        this.dataPointId = vo.njbGetDataPoint().getId();
        this.pointEventDetectorId = vo.getId();
        this.alarmLevel = vo.getAlarmLevel();
        if (!vo.isRtnApplicable()) {
            duplicateHandling = DuplicateHandling.ALLOW;
        }
    }

    @Override
    public EventSources getEventSource() {
        return EventSources.DATA_POINT;
    }

    @Override
    public int getDataSourceId() {
        if (dataSourceId == -1) {
            dataSourceId = dataPointDao.getDataPoint(dataPointId).getDataSourceId();
        }
        return dataSourceId;
    }

    @Override
    public int getDataPointId() {
        return dataPointId;
    }

    public int getPointEventDetectorId() {
        return pointEventDetectorId;
    }

    @Override
    public String toString() {
        return "DataPointEventType(dataPointId=" + dataPointId + ", detectorId=" + pointEventDetectorId + ")";
    }

    @Override
    public DuplicateHandling getDuplicateHandling() {
        return duplicateHandling;
    }

    public void setDuplicateHandling(DuplicateHandling duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    public int getReferenceId1() {
        return dataPointId;
    }

    public int getReferenceId2() {
        return pointEventDetectorId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pointEventDetectorId;
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
        DataPointEventType other = (DataPointEventType) obj;
        return pointEventDetectorId == other.pointEventDetectorId;
    }

    //
    // /
    // / Serialization
    // /
    //
    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("dataPointXID", dataPointDao.getDataPoint(dataPointId).getXid());
        map.put("detectorXID", dataPointDao.getDetectorXid(pointEventDetectorId));
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        dataPointId = getDataPointId(json, "dataPointXID");
        pointEventDetectorId = getPointEventDetectorId(json, dataPointId, "detectorXID");
    }

    @Override
    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

}
