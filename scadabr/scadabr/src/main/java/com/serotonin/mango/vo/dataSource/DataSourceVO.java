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

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteProperty;
import br.org.scadabr.json.JsonSerializable;
import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.dataSource.http.HttpImageDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpReceiverDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import com.serotonin.mango.vo.dataSource.internal.InternalDataSourceVO;
import com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO;
import com.serotonin.mango.vo.dataSource.persistent.PersistentDataSourceVO;
import com.serotonin.mango.vo.dataSource.sql.SqlDataSourceVO;
import com.serotonin.mango.vo.dataSource.virtual.VirtualDataSourceVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.vo.event.AlarmLevel;

abstract public class DataSourceVO<T extends DataSourceVO<T>> implements
        Serializable, Cloneable, JsonSerializable, ChangeComparable<T> {

    public enum Type {

        HTTP_RECEIVER(7, "dsEdit.httpReceiver", false) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new HttpReceiverDataSourceVO();
                    }
                },
        HTTP_RETRIEVER(11, "dsEdit.httpRetriever", false) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new HttpRetrieverDataSourceVO();
                    }
                },
        HTTP_IMAGE(15, "dsEdit.httpImage", false) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new HttpImageDataSourceVO();
                    }
                },
        INTERNAL(27, "dsEdit.internal", false) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new InternalDataSourceVO();
                    }
                },
        META(9, "dsEdit.meta", true) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new MetaDataSourceVO();
                    }
                },
        PERSISTENT(24, "dsEdit.persistent", false) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new PersistentDataSourceVO();
                    }
                },
        SQL(6, "dsEdit.sql", false) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new SqlDataSourceVO();
                    }
                },
        VIRTUAL(1, "dsEdit.virtual", true) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new VirtualDataSourceVO();
                    }
                };

        private Type(int id, String key, boolean display) {
            this.id = id;
            this.key = key;
            this.display = display;
        }

        private final int id;
        private final String key;
        private final boolean display;

        @Deprecated
        public int getId() {
            return id;
        }

        public String getKey() {
            return key;
        }

        public boolean isDisplay() {
            return display;
        }

        public abstract DataSourceVO<?> createDataSourceVO();

        public static Type valueOf(int id) {
            for (Type type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }

        public static Type valueOfIgnoreCase(String text) {
            for (Type type : values()) {
                if (type.name().equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return null;
        }

        public static List<String> getTypeList() {
            List<String> result = new ArrayList<>();
            for (Type type : values()) {
                result.add(type.name());
            }
            return result;
        }
    }

    public static final String XID_PREFIX = "DS_";

    public static DataSourceVO<?> createDataSourceVO(int typeId) {
        return Type.valueOf(typeId).createDataSourceVO();
    }

    public static String generateXid() {
        return Common.generateXid("DS_");
    }

    abstract public Type getType();

    abstract public LocalizableMessage getConnectionDescription();

    abstract public PointLocatorVO createPointLocator();

    abstract public DataSourceRT<T> createDataSourceRT();

    abstract public ExportCodes getEventCodes();

    final public List<EventTypeVO> getEventTypes() {
        List<EventTypeVO> eventTypes = new ArrayList<>();
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
    private String name;
    @JsonRemoteProperty
    private boolean enabled;
    private Map<Integer, AlarmLevel> alarmLevels = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
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

    public void setAlarmLevel(int eventId, AlarmLevel level) {
        alarmLevels.put(eventId, level);
    }

    public AlarmLevel getAlarmLevel(int eventId, AlarmLevel defaultLevel) {
        AlarmLevel level = alarmLevels.get(eventId);
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
        return createEventType(eventId, message, DuplicateHandling.IGNORE, AlarmLevel.URGENT);
    }

    protected EventTypeVO createEventType(int eventId,
            LocalizableMessage message, DuplicateHandling duplicateHandling,
            AlarmLevel defaultAlarmLevel) {
        return new EventTypeVO(EventSources.DATA_SOURCE, getId(),
                eventId, message, getAlarmLevel(eventId, defaultAlarmLevel),
                duplicateHandling);
    }

    public void validate(DwrResponseI18n response) {
        if (xid.isEmpty()) {
            response.addContextual("xid", "validate.required");
        } else if (!DataSourceDao.getInstance().isXidUnique(xid, id)) {
            response.addContextual("xid", "validate.xidUsed");
        } else if (xid.length() > 50) {
            response.addContextual("xid", "validate.notLongerThan", 50);
        }

        if (name.isEmpty()) {
            response.addContextual("dataSourceName", "validate.nameRequired");
        }
        if (name.length() > 40) {
            response.addContextual("dataSourceName", "validate.nameTooLong");
        }
    }

    public DataSourceVO<?> copy() {
        try {
            return (DataSourceVO<?>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    @Override
    //TODO is tis everytime an audit event ???
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
        final DataSourceVO fromVO = (DataSourceVO) from;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.head.name",
                fromVO.name, name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid",
                fromVO.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled",
                fromVO.enabled, enabled);

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
        final Map<Integer, Integer> _alarmLevels = new HashMap<>();
        for (Map.Entry<Integer, AlarmLevel> e : alarmLevels.entrySet()) {
            _alarmLevels.put(e.getKey(), e.getValue().mangoDbId);
        }
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
            alarmLevels = new HashMap<>();
        } else if (ver == 2) {
            enabled = in.readBoolean();
            final Map<Integer, Integer> _alarmLevels = (HashMap<Integer, Integer>) in.readObject();
            alarmLevels = new HashMap<>();
            for (Map.Entry<Integer, Integer> e : _alarmLevels.entrySet()) {
                alarmLevels.put(e.getKey(), AlarmLevel.fromMangoDbId(e.getValue()));
            }

        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("xid", xid);
        map.put("type", getType().name());

        ExportCodes eventCodes = getEventCodes();
        if (eventCodes != null && eventCodes.size() > 0) {
            Map<String, String> alarmCodeLevels = new HashMap<>();

            for (int i = 0; i < eventCodes.size(); i++) {
                int eventId = eventCodes.getId(i);
                AlarmLevel level = getAlarmLevel(eventId, AlarmLevel.URGENT);
                alarmCodeLevels.put(eventCodes.getCode(eventId), level.getName());
            }

            map.put("alarmLevels", alarmCodeLevels);
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json)
            throws JsonException {
        // Can't change the type.

        JsonObject alarmCodeLevels = json.getJsonObject("alarmLevels");
        if (alarmCodeLevels != null) {
            ExportCodes eventCodes = getEventCodes();
            if (eventCodes != null && eventCodes.size() > 0) {
                for (String code : alarmCodeLevels.getProperties().keySet()) {
                    int eventId = eventCodes.getId(code);
                    if (!eventCodes.isValidId(eventId)) {
                        throw new LocalizableJsonException(
                                "emport.error.eventCode", code,
                                eventCodes.getCodeList());
                    }

                    String text = alarmCodeLevels.getString(code);
                    try {
                        AlarmLevel level = AlarmLevel.valueOf(text);
                        setAlarmLevel(eventId, level);
                    } catch (Exception e) {
                        throw new LocalizableJsonException(
                                "emport.error.alarmLevel", text, code,
                                AlarmLevel.nameValues());
                    }

                }
            }
        }
    }

    protected void serializeUpdatePeriodType(Map<String, Object> map,
            TimePeriods updatePeriodType) {
        map.put("updatePeriodType", updatePeriodType.name());
    }

    protected TimePeriods deserializeUpdatePeriodType(JsonObject json)
            throws JsonException {
        String text = json.getString("updatePeriodType");
        if (text == null) {
            return null;
        }
        TimePeriods value;
        try {
            value = TimePeriods.valueOf(text);
        } catch (Exception e) {
            throw new LocalizableJsonException("emport.error.invalid",
                    "updatePeriodType", text,
                    TimePeriods.values());
        }

        return value;
    }
}
