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
package com.serotonin.mango.vo.dataSource.pachube;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.json.JsonRemoteProperty;
import br.org.scadabr.json.JsonSerializable;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.pachube.PachubePointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.util.StringUtils;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;

@JsonRemoteEntity
public class PachubePointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {

    @Override
    public PointLocatorRT createRuntime() {
        return new PachubePointLocatorRT(this);
    }

    @Override
    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessageImpl("dsEdit.pachube.dpconn", feedId, dataStreamId);
    }

    @JsonRemoteProperty
    private int feedId;
    @JsonRemoteProperty
    private String dataStreamId;
    private int dataTypeId;
    @JsonRemoteProperty
    private String binary0Value;
    @JsonRemoteProperty
    private boolean settable;

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public String getDataStreamId() {
        return dataStreamId;
    }

    public void setDataStreamId(String dataStreamId) {
        this.dataStreamId = dataStreamId;
    }

    public int getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public String getBinary0Value() {
        return binary0Value;
    }

    public void setBinary0Value(String binary0Value) {
        this.binary0Value = binary0Value;
    }

    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    public void validate(DwrResponseI18n response) {
        if (feedId <= 0) {
            response.addContextualMessage("feedId", "validate.invalidValue");
        }

        if (dataStreamId.isEmpty()) {
            response.addContextualMessage("dataStreamId", "validate.required");
        }

        if (!DataTypes.CODES.isValidId(dataTypeId)) {
            response.addContextualMessage("dataTypeId", "validate.invalidValue");
        }
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.pachube.feedId", feedId);
        AuditEventType.addPropertyMessage(list, "dsEdit.pachube.dataStreamId", dataStreamId);
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", dataTypeId);
        AuditEventType.addPropertyMessage(list, "dsEdit.pachube.binaryZeroValue", binary0Value);
        AuditEventType.addPropertyMessage(list, "dsEdit.settable", settable);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        PachubePointLocatorVO from = (PachubePointLocatorVO) o;
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.dataTypeId, dataTypeId);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.pachube.feedId", from.feedId, feedId);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.pachube.dataStreamId", from.dataStreamId,
                dataStreamId);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.pachube.binaryZeroValue", from.binary0Value,
                binary0Value);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.settable", from.settable, settable);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(feedId);
        SerializationHelper.writeSafeUTF(out, dataStreamId);
        out.writeInt(dataTypeId);
        SerializationHelper.writeSafeUTF(out, binary0Value);
        out.writeBoolean(settable);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            feedId = in.readInt();
            dataStreamId = SerializationHelper.readSafeUTF(in);
            dataTypeId = in.readInt();
            binary0Value = SerializationHelper.readSafeUTF(in);
            settable = in.readBoolean();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        Integer value = deserializeDataType(json, DataTypes.IMAGE);
        if (value != null) {
            dataTypeId = value;
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        serializeDataType(map);
    }
}