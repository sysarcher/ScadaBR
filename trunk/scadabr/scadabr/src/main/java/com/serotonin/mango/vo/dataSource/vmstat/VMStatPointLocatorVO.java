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
package com.serotonin.mango.vo.dataSource.vmstat;

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
import com.serotonin.mango.rt.dataSource.vmstat.VMStatPointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class VMStatPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {

    @JsonRemoteProperty(alias="attributeId")
    private VMStatAttributes attribute = VMStatAttributes.CPU_ID;

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new VMStatPointLocatorRT(this);
    }

    @Override
    public MangoDataType getMangoDataType() {
        return MangoDataType.NUMERIC;
    }

    public VMStatAttributes getAttribute() {
        return attribute;
    }

    public void setAttribute(VMStatAttributes attribute) {
        this.attribute = attribute;
    }

    @Override
    public void validate(DwrResponseI18n response) {
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.vmstat.attribute", attribute);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        VMStatPointLocatorVO from = (VMStatPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.vmstat.attribute", from.attribute, attribute);
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
        out.writeInt(attribute.mangoId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1)
            attribute = VMStatAttributes.fromMangoId(in.readInt());
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
    }

    @Override
    public LocalizableMessage getConfigurationDescription() {
        return attribute.getMessageI18n();
    }
}
