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
package com.serotonin.mango.vo.dataSource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

abstract public class DataSourceVO<T extends DataSourceVO<?>> implements
        Serializable, Cloneable, JsonSerializable, ChangeComparable<T> {

    public static final String XID_PREFIX = "DS_";
    @Autowired
    private DataSourceDao dataSourceDao;

    public static DataSourceVO<?> createDataSourceVO(DataSourceRegistry dataSourceType) {
        return dataSourceType.createDataSourceVO();
    }

    public static String generateXid() {
        return Common.generateXid("DS_");
    }

    abstract public DataSourceRegistry getType();

    abstract public LocalizableMessage getConnectionDescription();

    abstract public PointLocatorVO createPointLocator();

    abstract public DataSourceRT createDataSourceRT();

    abstract public ExportCodes getEventCodes();

    final public List<EventTypeVO> getEventTypes() {
        List<EventTypeVO> eventTypes = new ArrayList();
        addEventTypes(eventTypes);
        return eventTypes;
    }

    abstract protected void addEventTypes(List<EventTypeVO> eventTypes);

    public boolean isNew() {
        return id == Common.NEW_ID;
    }
    private int id = Common.NEW_ID;
    private String xid;
    @JsonRemoteProperty
    private String name = getClass().getSimpleName();
    @JsonRemoteProperty
    private boolean enabled;
    @JsonRemoteProperty
    private Map<Integer, AlarmLevels> alarmLevels = new HashMap();

    public DataSourceVO<T> clone() {
        try {
            return (DataSourceVO<T>) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new ShouldNeverHappenException(ex);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlarmLevel(int eventId, AlarmLevels level) {
        alarmLevels.put(eventId, level);
    }

    public AlarmLevels getAlarmLevel(int eventId, AlarmLevels defaultLevel) {
        AlarmLevels level = alarmLevels.get(eventId);
        if (level == null) {
            return defaultLevel;
        }
        return level;
    }

    public EventTypeVO getEventType(int eventId) {
        for (EventTypeVO vo : getEventTypes()) {
            if (vo.getTypeRef2() == eventId) {
                return vo;
            }
        }
        return null;
    }

    protected EventTypeVO createEventType(int eventId,
            LocalizableMessage message) {
        return createEventType(eventId, message,
                EventType.DuplicateHandling.IGNORE, AlarmLevels.URGENT);
    }

    protected EventTypeVO createEventType(int eventId,
            LocalizableMessage message, int duplicateHandling,
            AlarmLevels defaultAlarmLevel) {
        return new EventTypeVO(EventType.EventSources.DATA_SOURCE, getId(),
                eventId, message, getAlarmLevel(eventId, defaultAlarmLevel),
                duplicateHandling);
    }

    public void validate(DwrResponseI18n response) {
        if (StringUtils.isEmpty(xid)) {
            response.addContextualMessage("xid", "validate.required");
        } else if (!dataSourceDao.isXidUnique(xid, id)) {
            response.addContextualMessage("xid", "validate.xidUsed");
        } else if (StringUtils.isLengthGreaterThan(xid, 50)) {
            response.addContextualMessage("xid", "validate.notLongerThan", 50);
        }

        if (StringUtils.isEmpty(name)) {
            response.addContextualMessage("dataSourceName",
                    "validate.nameRequired");
        }
        if (StringUtils.isLengthGreaterThan(name, 40)) {
            response.addContextualMessage("dataSourceName",
                    "validate.nameTooLong");
        }
    }

    protected String getMessage(ResourceBundle bundle, String key,
            Object... args) {
        return new LocalizableMessage(key, args).getLocalizedMessage(bundle);
    }

    @Override
    public String getTypeKey() {
        return "event.audit.dataSource";
    }

    @Override
    public final void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.head.name", name);
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "common.enabled", enabled);

        addPropertiesImpl(list);
    }

    @Override
    public final void addPropertyChanges(List<LocalizableMessage> list, T from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.head.name",
                from.getName(), name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid",
                from.getXid(), xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled",
                from.isEnabled(), enabled);

        addPropertyChangesImpl(list, from);
    }

    abstract protected void addPropertiesImpl(List<LocalizableMessage> list);

    abstract protected void addPropertyChangesImpl(
            List<LocalizableMessage> list, T from);
    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeBoolean(enabled);
        out.writeObject(alarmLevels);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be
        // elegantly handled.
        if (ver == 1) {
            enabled = in.readBoolean();
            alarmLevels = new HashMap();
        } else if (ver == 2) {
            enabled = in.readBoolean();
            alarmLevels = (HashMap<Integer, AlarmLevels>) in.readObject();
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("xid", xid);
        map.put("type", getType().name());
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json)
            throws JsonException {
        // Wont't change the type here.
        // xid is handled elswhere
    }

    protected void serializeUpdatePeriodType(Map<String, Object> map,
            int updatePeriodType) {
        map.put("updatePeriodType",
                Common.TIME_PERIOD_CODES.getCode(updatePeriodType));
    }

    protected Integer deserializeUpdatePeriodType(JsonObject json)
            throws JsonException {
        String text = json.getString("updatePeriodType");
        if (text == null) {
            return null;
        }

        int value = Common.TIME_PERIOD_CODES.getId(text);
        if (value == -1) {
            throw new LocalizableJsonException("emport.error.invalid",
                    "updatePeriodType", text,
                    Common.TIME_PERIOD_CODES.getCodeList());
        }

        return value;
    }

    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.id;
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSourceVO<T> other = (DataSourceVO<T>) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s [id=%d, xid=%s, name=%s]", getClass().getName(), id, xid, name);
    }
}
