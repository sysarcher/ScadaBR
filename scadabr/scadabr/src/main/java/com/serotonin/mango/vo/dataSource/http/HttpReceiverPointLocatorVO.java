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
package com.serotonin.mango.vo.dataSource.http;

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
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.http.HttpReceiverPointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class HttpReceiverPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {
    public boolean isSettable() {
        return false;
    }

    public PointLocatorRT createRuntime() {
        return new HttpReceiverPointLocatorRT(this);
    }

    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessage("dsEdit.httpReceiver.dpconn", parameterName);
    }

    @JsonRemoteProperty
    private String parameterName;
    @JsonRemoteProperty(alias=MangoDataType.ALIAS_DATA_TYPE)
    private MangoDataType mangoDataType = MangoDataType.UNKNOWN;
    @JsonRemoteProperty
    private String binary0Value;

    @Override
    public MangoDataType getMangoDataType() {
        return mangoDataType;
    }

    public void setMangoDataType(MangoDataType mangoDataType) {
        this.mangoDataType = mangoDataType;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getBinary0Value() {
        return binary0Value;
    }

    public void setBinary0Value(String binary0Value) {
        this.binary0Value = binary0Value;
    }

    public void validate(DwrResponseI18n response) {
        if (StringUtils.isEmpty(parameterName))
            response.addContextualMessage("parameterName", "validate.required");
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.httpReceiver.httpParamName", parameterName);
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", mangoDataType);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpReceiver.binaryZeroValue", binary0Value);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        HttpReceiverPointLocatorVO from = (HttpReceiverPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpReceiver.httpParamName", from.parameterName,
                parameterName);
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.mangoDataType, mangoDataType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpReceiver.binaryZeroValue", from.binary0Value,
                binary0Value);
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
        SerializationHelper.writeSafeUTF(out, parameterName);
        out.writeInt(mangoDataType.mangoId);
        SerializationHelper.writeSafeUTF(out, binary0Value);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            parameterName = SerializationHelper.readSafeUTF(in);
            mangoDataType = MangoDataType.fromMangoId(in.readInt());
            binary0Value = SerializationHelper.readSafeUTF(in);
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
    }
}
