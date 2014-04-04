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
package com.serotonin.mango.vo.dataSource.galil;

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
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.galil.PointTypeRT;
import com.serotonin.mango.rt.dataSource.galil.VariablePointTypeRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.LocalizableJsonException;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class VariablePointTypeVO extends PointTypeVO {

    @JsonRemoteProperty
    private String variableName = "";
    private int dataTypeId = DataTypes.NUMERIC;

    @Override
    public PointTypeRT createRuntime() {
        return new VariablePointTypeRT(this);
    }

    @Override
    public int typeId() {
        return Types.VARIABLE;
    }

    @Override
    public int getDataTypeId() {
        return dataTypeId;
    }

    @Override
    public LocalizableMessage getDescription() {
        return new LocalizableMessageImpl("dsEdit.galil.pointType.variable");
    }

    @Override
    public boolean isSettable() {
        return true;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        if (!DataTypes.CODES.isValidId(dataTypeId, DataTypes.IMAGE)) {
            response.addContextual("dataTypeId", "validate.invalidValue");
        }
        if (variableName.isEmpty()) {
            response.addContextual("variablePointType.variableName", "validate.required");
        }
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.galil.varName", variableName);
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", dataTypeId);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        VariablePointTypeVO from = (VariablePointTypeVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.galil.varName", from.variableName, variableName);
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.dataTypeId, dataTypeId);
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
        SerializationHelper.writeSafeUTF(out, variableName);
        out.writeInt(dataTypeId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            variableName = SerializationHelper.readSafeUTF(in);
            dataTypeId = in.readInt();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);

        String text = json.getString("dataType");
        if (text != null) {
            dataTypeId = DataTypes.CODES.getId(text);
            if (!DataTypes.CODES.isValidId(dataTypeId, DataTypes.IMAGE)) {
                throw new LocalizableJsonException("emport.error.invalid", "dataType", text, DataTypes.CODES
                        .getCodeList(DataTypes.IMAGE));
            }
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("dataType", DataTypes.CODES.getCode(dataTypeId));
    }
}
