package com.serotonin.mango.rt.event.type;

import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.db.dao.MaintenanceEventDao;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class MaintenanceEventType extends EventType {

    @Autowired
    private MaintenanceEventDao maintenanceEventDao;
    private int maintenanceId;
    private AlarmLevel alarmLevel;

    public MaintenanceEventType() {
        // Required for reflection.
    }

    @Deprecated
    public MaintenanceEventType(int maintenanceId) {
        this.maintenanceId = maintenanceId;
//        this.alarmLevel = vo.getAlarmLevel();
    }

    public MaintenanceEventType(MaintenanceEventVO vo) {
        this.maintenanceId = vo.getId();
        this.alarmLevel = vo.getAlarmLevel();
    }

    @Override
    public EventSources getEventSource() {
        return EventSources.MAINTENANCE;
    }

    public int getMaintenanceId() {
        return maintenanceId;
    }

    @Override
    public String toString() {
        return "MaintenanceEventType(maintenanceId=" + maintenanceId + ")";
    }

    @Override
    public DuplicateHandling getDuplicateHandling() {
        return DuplicateHandling.IGNORE;
    }

    public int getReferenceId1() {
        return maintenanceId;
    }

    public int getReferenceId2() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + maintenanceId;
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
        MaintenanceEventType other = (MaintenanceEventType) obj;
        if (maintenanceId != other.maintenanceId) {
            return false;
        }
        return true;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("XID", maintenanceEventDao.getMaintenanceEvent(maintenanceId).getXid());
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        maintenanceId = getMaintenanceEventId(json, "XID");
    }

    @Override
    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

    @Override
    public boolean isStateful() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
