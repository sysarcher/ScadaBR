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
package com.serotonin.mango.vo.dataSource.meta;

import br.org.scadabr.DataType;
import br.org.scadabr.ShouldNeverHappenException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.scadabr.db.IntValuePair;
import br.org.scadabr.json.JsonArray;
import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.json.JsonRemoteProperty;
import br.org.scadabr.json.JsonSerializable;
import br.org.scadabr.json.JsonValue;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.CronParser;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.meta.MetaPointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.datasource.meta.UpdateEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataSource.meta.MetaDataSourceRT;
import java.text.ParseException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
@Configurable
public class MetaPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {

    @Autowired
    private DataPointDao dataPointDao;

    private List<IntValuePair> context = new ArrayList<>();
    @JsonRemoteProperty
    private String script;
    private DataType dataType;
    @JsonRemoteProperty
    private boolean settable;
    private UpdateEvent updateEvent = UpdateEvent.CONTEXT_UPDATE;
    @JsonRemoteProperty
    private String updateCronPattern;
    @JsonRemoteProperty
    private int executionDelaySeconds;
    private String name;

    MetaPointLocatorVO() {

    }

    MetaPointLocatorVO(DataType dataType) {
        this();
        this.dataType = dataType;
        this.name = getClass().getSimpleName();
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new MetaPointLocatorRT(this);
    }

    @Override
    public LocalizableMessage getConfigurationDescription() {
        if (script == null || script.length() < 40) {
            return new LocalizableMessageImpl("common.default", "'" + script + "'");
        } else {
            return new LocalizableMessageImpl("common.default", "'" + script.substring(0, 40) + "'");
        }
    }

    public List<IntValuePair> getContext() {
        return context;
    }

    public void setContext(List<IntValuePair> context) {
        this.context = context;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public int getExecutionDelaySeconds() {
        return executionDelaySeconds;
    }

    public void setExecutionDelaySeconds(int executionDelaySeconds) {
        this.executionDelaySeconds = executionDelaySeconds;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    public UpdateEvent getUpdateEvent() {
        return updateEvent;
    }

    public List<UpdateEvent> getUpdateEvents() {
        return Arrays.asList(UpdateEvent.values());
    }

    public void setUpdateEvent(UpdateEvent updateEvent) {
        this.updateEvent = updateEvent;
    }

    public String getUpdateCronPattern() {
        return updateCronPattern;
    }

    public void setUpdateCronPattern(String updateCronPattern) {
        this.updateCronPattern = updateCronPattern;
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.pointDataType", dataType);
        AuditEventType.addPropertyMessage(list, "dsEdit.settable", settable);
        AuditEventType.addPropertyMessage(list, "dsEdit.meta.scriptContext", contextToString());
        AuditEventType.addPropertyMessage(list, "dsEdit.meta.script", script);
        AuditEventType.addPropertyMessage(list, "dsEdit.meta.event", updateEvent);
        if (updateEvent == updateEvent.CRON) {
            AuditEventType.addPropertyMessage(list, "dsEdit.meta.event.cron", updateCronPattern);
        }
        AuditEventType.addPropertyMessage(list, "dsEdit.meta.delay", executionDelaySeconds);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        MetaPointLocatorVO from = (MetaPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.pointDataType", from.dataType, dataType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.settable", from.settable, settable);
        if (!context.equals(context)) {
            AuditEventType.addPropertyChangeMessage(list, "dsEdit.meta.scriptContext", from.contextToString(),
                    contextToString());
        }
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.meta.script", from.script, script);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.meta.event", from.updateEvent, updateEvent);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.meta.event.cron", from.updateCronPattern, updateCronPattern);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.meta.delay", from.executionDelaySeconds,
                executionDelaySeconds);
    }

    private String contextToString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (IntValuePair ivp : context) {
            DataPointVO dp = dataPointDao.getDataPoint(ivp.getKey());
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            if (dp == null) {
                sb.append("?=");
            } else {
                sb.append(dp.getName()).append("=");
            }
            sb.append(ivp.getValue());
        }
        return sb.toString();
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 5;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeObject(name);
        out.writeObject(context);
        SerializationHelper.writeSafeUTF(out, script);
        out.writeInt(dataType.mangoDbId);
        out.writeBoolean(settable);
        out.writeInt(updateEvent.getId());
        SerializationHelper.writeSafeUTF(out, updateCronPattern);
        out.writeInt(executionDelaySeconds);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            context = new ArrayList<>();
            Map<Integer, String> ctxMap = (Map<Integer, String>) in.readObject();
            for (Map.Entry<Integer, String> point : ctxMap.entrySet()) {
                context.add(new IntValuePair(point.getKey(), point.getValue()));
            }

            script = SerializationHelper.readSafeUTF(in);
            dataType = DataType.fromMangoDbId(in.readInt());
            settable = false;
            updateEvent = UpdateEvent.fromId(in.readInt());
            updateCronPattern = "";
            executionDelaySeconds = in.readInt();
        } else if (ver == 2) {
            context = (List<IntValuePair>) in.readObject();
            script = SerializationHelper.readSafeUTF(in);
            dataType = DataType.fromMangoDbId(in.readInt());
            settable = false;
            updateEvent = UpdateEvent.fromId(in.readInt());
            updateCronPattern = "";
            executionDelaySeconds = in.readInt();
        } else if (ver == 3) {
            context = (List<IntValuePair>) in.readObject();
            script = SerializationHelper.readSafeUTF(in);
            dataType = DataType.fromMangoDbId(in.readInt());
            settable = false;
            updateEvent = UpdateEvent.fromId(in.readInt());
            updateCronPattern = SerializationHelper.readSafeUTF(in);
            executionDelaySeconds = in.readInt();
        } else if (ver == 4) {
            context = (List<IntValuePair>) in.readObject();
            script = SerializationHelper.readSafeUTF(in);
            dataType = DataType.fromMangoDbId(in.readInt());
            settable = in.readBoolean();
            updateEvent = UpdateEvent.fromId(in.readInt());
            updateCronPattern = SerializationHelper.readSafeUTF(in);
            executionDelaySeconds = in.readInt();
        } else if (ver == 5) {
            name = (String) in.readObject();
            context = (List<IntValuePair>) in.readObject();
            script = SerializationHelper.readSafeUTF(in);
            dataType = DataType.fromMangoDbId(in.readInt());
            settable = in.readBoolean();
            updateEvent = UpdateEvent.fromId(in.readInt());
            updateCronPattern = SerializationHelper.readSafeUTF(in);
            executionDelaySeconds = in.readInt();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        DataType value = deserializeDataType(json, EnumSet.of(DataType.IMAGE));
        if (value != null) {
            dataType = value;
        }

        String text = json.getString("updateEvent");
        if (text != null) {
            try {
                updateEvent = UpdateEvent.valueOf(text);
            } catch (Exception e) {
                throw new LocalizableJsonException("emport.error.invalid", "updateEvent", text,
                        UpdateEvent.values());
            }
        }

        JsonArray jsonContext = json.getJsonArray("context");
        if (jsonContext != null) {
            context.clear();

            for (JsonValue jv : jsonContext.getElements()) {
                JsonObject jo = jv.toJsonObject();
                String xid = jo.getString("dataPointXid");
                if (xid == null) {
                    throw new LocalizableJsonException("emport.error.meta.missing", "dataPointXid");
                }

                DataPointVO dp = dataPointDao.getDataPoint(xid);
                if (dp == null) {
                    throw new LocalizableJsonException("emport.error.missingPoint", xid);
                }

                String var = jo.getString("varName");
                if (var == null) {
                    throw new LocalizableJsonException("emport.error.meta.missing", "varName");
                }

                context.add(new IntValuePair(dp.getId(), var));
            }
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        serializeDataType(map);

        map.put("updateEvent", updateEvent.name());

        List<Map<String, Object>> pointList = new ArrayList<>();
        for (IntValuePair p : context) {
            DataPointVO dp = dataPointDao.getDataPoint(p.getKey());
            if (dp != null) {
                Map<String, Object> point = new HashMap<>();
                pointList.add(point);
                point.put("varName", p.getValue());
                point.put("dataPointXid", dp.getXid());
            }
        }
        map.put("context", pointList);
    }

    public boolean isScriptEmpty() {
        return script == null ? true : script.isEmpty();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public CronExpression getCronExpression() {
        try {
        switch (updateEvent) {
            case CONTEXT_UPDATE:
                throw new ShouldNeverHappenException("Context update has no cron pattern");
            case CRON:
                return new CronParser().parse(updateCronPattern, TimeZone.getTimeZone("UTC"));
            default:
                return new CronParser().parse(updateEvent.getCronPattern(), TimeZone.getTimeZone("UTC"));
        }
        } catch (ParseException pe) {
            throw new ShouldNeverHappenException(pe.getMessage());
        }
    }
}
