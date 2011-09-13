/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.vo.dataSource.fhz4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import net.sf.fhz4j.FhzProtocol;
import org.slf4j.Logger;

import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.mango.rt.dataSource.fhz4j.Fhz4JPointLocatorRT;
import java.util.LinkedHashMap;
import net.sf.fhz4j.scada.ScadaProperty;
import org.slf4j.LoggerFactory;

// Container to move data with json and ajax so ony basic datatypes
@JsonRemoteEntity
public class Fhz4JPointLocatorVO<T extends ScadaProperty> extends AbstractPointLocatorVO implements JsonSerializable {

    private final static Logger LOG = LoggerFactory.getLogger(Fhz4JPointLocatorVO.class);
    private T property;

    Fhz4JPointLocatorVO() {
        super();
    }
    private boolean settable = false;

    public String defaultName() {
        return property == null ? "Fhz4J dataPoint" : getProperty().getLabel();
    }


    @Override
    public int getDataTypeId() {
        if (property == null) {
            return DataTypes.UNKNOWN;
        }
        switch (property.getDataType()) {
            case BOOLEAN:
                return DataTypes.BINARY;
            case BYTE:
                return DataTypes.MULTISTATE;
            case CHAR:
                return DataTypes.ALPHANUMERIC;
            case DOUBLE:
                return DataTypes.NUMERIC;
            case FLOAT:
                return DataTypes.NUMERIC;
            case LONG:
                return DataTypes.MULTISTATE;
            case INT:
                return DataTypes.MULTISTATE;
            case SHORT:
                return DataTypes.MULTISTATE;
            case STRING:
                return DataTypes.ALPHANUMERIC;
            case TIME:
                return DataTypes.ALPHANUMERIC;
            default:
                throw new RuntimeException("Cant find datatype of " + property);
        }

    }

    @Override
    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessage("dsEdit.openv4j", "Something", "I dont know");
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    @Override
    public boolean isSettable() {
        return settable;
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new Fhz4JPointLocatorRT(this);
    }

    @Override
    public void validate(DwrResponseI18n response) {
        // no op
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", settable);
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", property);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        Fhz4JPointLocatorVO from = (Fhz4JPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.settable, settable);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.property, property);
    }
    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int serialVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(serialVersion);
        out.writeBoolean(settable);
        out.writeObject(property);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        switch (ver) {
            case 1:
                settable = in.readBoolean();
                property = (T)in.readObject();
                break;
            default:
                throw new RuntimeException("Cant handle version");
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) {
        //TODO
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("settable", settable);
        Map<String, Object> locatorMap = new LinkedHashMap<String, Object>();
        map.put("fhzProtocol", getFhzProtocol());
        map.put("fhzProperty", property);
 }

    public FhzProtocol getFhzProtocol() {
        return FhzProtocol.UNKNOWN;
    }

    /**
     * @return the property
     */
    public T getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(T property) {
        this.property = property;
    }

}
