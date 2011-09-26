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

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataSource.galil.PointTypeRT;
import com.serotonin.mango.rt.dataSource.galil.VariablePointTypeRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class VariablePointTypeVO extends PointTypeVO {
    @JsonRemoteProperty
    private String variableName = "";
    @JsonRemoteProperty(alias=MangoDataType.ALIAS_DATA_TYPE)
    private MangoDataType mangoDataType = MangoDataType.NUMERIC;

    @Override
    public PointTypeRT createRuntime() {
        return new VariablePointTypeRT(this);
    }

    @Override
    public int typeId() {
        return Types.VARIABLE;
    }

    @Override
    public MangoDataType getMangoDataType() {
        return mangoDataType;
    }

    @Override
    public LocalizableMessage getDescription() {
        return new LocalizableMessage("dsEdit.galil.pointType.variable");
    }

    @Override
    public boolean isSettable() {
        return true;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        if (mangoDataType != MangoDataType.IMAGE)
            response.addContextualMessage("mangoDataType", "validate.invalidValue");
        if (StringUtils.isEmpty(variableName))
            response.addContextualMessage("variablePointType.variableName", "validate.required");
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setMangoDataType(MangoDataType mangoDataType) {
        this.mangoDataType = mangoDataType;
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.galil.varName", variableName);
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", mangoDataType);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        VariablePointTypeVO from = (VariablePointTypeVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.galil.varName", from.variableName, variableName);
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.mangoDataType, mangoDataType);
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
        out.writeInt(mangoDataType.mangoId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            variableName = SerializationHelper.readSafeUTF(in);
            mangoDataType = MangoDataType.fromMangoId(in.readInt());
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
    }
}
