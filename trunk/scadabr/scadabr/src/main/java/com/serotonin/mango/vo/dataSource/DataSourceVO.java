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

import br.org.scadabr.ScadaBrConstants;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.scadabr.ShouldNeverHappenException;

import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.dataSource.http.HttpImageDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpReceiverDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.vo.event.AlarmLevel;
import br.org.scadabr.vo.event.type.DataSourceEventKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.mango.rt.event.type.DataSourceEventType;
import java.util.Set;
import org.springframework.validation.Validator;

abstract public class DataSourceVO<T extends DataSourceVO<T>> implements
        Serializable, Cloneable, ChangeComparable<T> {

    public abstract Validator createValidator();

    private void fillEventTypeMap() {
        this.eventTypeMap = (Map<DataSourceEventKey, DataSourceEventType>) createEventKeyMap();
        for (DataSourceEventKey key : createEventKeySet()) {
            eventTypeMap.put(key, new DataSourceEventType(id, key, key.getDefaultAlarmLevel()));
        }
    }

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
        META(9, "dsEdit.meta", true) {
                    @Override
                    public DataSourceVO<?> createDataSourceVO() {
                        return new MetaDataSourceVO();
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

    private Map<DataSourceEventKey, DataSourceEventType> eventTypeMap;

    abstract public Type getType();

    abstract public LocalizableMessage getConnectionDescription();

    abstract public PointLocatorVO createPointLocator();

    abstract public DataSourceRT<T> createDataSourceRT();

    public DataSourceVO() {
        fillEventTypeMap();
    }

    @JsonIgnore
    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
    }

    private int id = ScadaBrConstants.NEW_ID;
    private String xid;

    private String name = this.getClass().getSimpleName();

    private boolean enabled;

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
        // replace id with righth id...
        for (DataSourceEventKey key : eventTypeMap.keySet()) {
            DataSourceEventType dsEvt = eventTypeMap.get(key);
            eventTypeMap.put(key, new DataSourceEventType(id, key, dsEvt.getAlarmLevel()));
        }
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
//TODO events??
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

//TODO events??
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
        for (Map.Entry<DataSourceEventKey, DataSourceEventType> e : eventTypeMap.entrySet()) {
            _alarmLevels.put(e.getKey().getId(), e.getValue().getAlarmLevel().getId());
        }
        out.writeObject(_alarmLevels);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {

        int ver = in.readInt();
        // Switch on the version of the class so that version changes can be
        // elegantly handled.
        if (ver == 1) {
            enabled = in.readBoolean();
            fillEventTypeMap();
        } else if (ver == 2) {
            enabled = in.readBoolean();
            final Map<Integer, Integer> _alarmLevels = (HashMap<Integer, Integer>) in.readObject();
            this.eventTypeMap = (Map<DataSourceEventKey, DataSourceEventType>) createEventKeyMap();
            for (DataSourceEventKey key : createEventKeySet()) {
                final Integer alId = _alarmLevels.get(key.getId());
                eventTypeMap.put(key, new DataSourceEventType(id, key, alId != null ? AlarmLevel.fromId(alId) : key.getDefaultAlarmLevel()));
            }
        }
        fillEventTypeMap();
    }

    /**
     * get all Types for configuration
     *
     * @return
     */
    public abstract <K extends DataSourceEventKey> Set<K> createEventKeySet();

    /**
     * Create a optimized map i.e. EnumMap ...
     *
     * @return
     */
    public abstract <K extends DataSourceEventKey> Map<K, ?> createEventKeyMap();

    /**
     * Get the type of a Key
     *
     * @param key
     * @return
     */
    public DataSourceEventType getEventType(DataSourceEventKey key) {
        return eventTypeMap.get(key);
    }

}
