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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import net.sf.fhz4j.Fhz1000;
import net.sf.fhz4j.FhzDeviceTypes;
import net.sf.fhz4j.FhzProperty;
import net.sf.fhz4j.fht.FhtProperty;
import com.serotonin.mango.rt.dataSource.fhz4j.Fhz4JPointLocatorRT;

// Container to move data with json and ajax so ony basic datatypes
@JsonRemoteEntity
public class Fhz4JPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {

    private final static Log LOG = LogFactory.getLog(Fhz4JPointLocatorVO.class);
    @JsonRemoteProperty
    private short deviceHousecode;
    @JsonRemoteProperty
    private FhzDeviceTypes fhzDeviceType;
    @JsonRemoteProperty
    private FhzProperty fhzProperty;
    @JsonRemoteProperty
    private boolean settable = false;
    private String fhzDeviceTypeLabel;
    private String fhzPropertyLabel;

    @Override
    public int getDataTypeId() {
        switch (fhzDeviceType) {
            case FHT_8:
            case FHT_80B:
                return getDataTypeOfFhtProperty((FhtProperty) fhzProperty);
            default:
                return DataTypes.UNKNOWN;
        }
    }

    public int getDataTypeOfFhtProperty(FhtProperty fhtProperty) {
        switch (fhtProperty) {
            case VALVE:
            case VALVE_1:
            case VALVE_2:
            case VALVE_3:
            case VALVE_4:
            case VALVE_5:
            case VALVE_6:
            case VALVE_7:
            case VALVE_8:
                return DataTypes.NUMERIC;
            case DESIRED_TEMP:
                return DataTypes.NUMERIC;
            case MEASURED_LOW:
            case MEASURED_HIGH:
                return DataTypes.NUMERIC;
            case MO_FROM_1:
            case MO_TO_1:
            case MO_FROM_2:
            case MO_TO_2:
            case TUE_FROM_1:
            case TUE_TO_1:
            case TUE_FROM_2:
            case TUE_TO_2:
            case WED_FROM_1:
            case WED_TO_1:
            case WED_FROM_2:
            case WED_TO_2:
            case THU_FROM_1:
            case THU_TO_1:
            case THU_FROM_2:
            case THU_TO_2:
            case FRI_FROM_1:
            case FRI_TO_1:
            case FRI_FROM_2:
            case FRI_TO_2:
            case SAT_FROM_1:
            case SAT_TO_1:
            case SAT_FROM_2:
            case SAT_TO_2:
            case SUN_FROM_1:
            case SUN_TO_1:
            case SUN_FROM_2:
            case SUN_TO_2:
                return DataTypes.ALPHANUMERIC;
            default:
                return DataTypes.NUMERIC;
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
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", deviceHousecode);
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", fhzDeviceType);
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", fhzProperty);
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", settable);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        Fhz4JPointLocatorVO from = (Fhz4JPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.deviceHousecode, deviceHousecode);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.fhzDeviceType, fhzDeviceType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.fhzProperty, fhzProperty);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.settable, settable);
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
        out.writeShort(deviceHousecode);
        out.writeObject(fhzDeviceType);
        out.writeObject(fhzProperty);
        out.writeBoolean(settable);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        switch (ver) {
            case 1:
                deviceHousecode = in.readShort();
                fhzDeviceType = (FhzDeviceTypes) in.readObject();
                fhzProperty = (FhzProperty) in.readObject();
                settable = in.readBoolean();
                break;
            default:
                throw new RuntimeException("Cant handle version");
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) {
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
    }

    /**
     * @return the deviceHousecode
     */
    public short getDeviceHousecode() {
        return deviceHousecode;
    }

    /**
     * @param deviceHousecode the deviceHousecode to set
     */
    public void setDeviceHousecode(short deviceHousecode) {
        this.deviceHousecode = deviceHousecode;
    }

    /**
     * @return the fhzDeviceType
     */
    public FhzDeviceTypes getFhzDeviceType() {
        return fhzDeviceType;
    }

    /**
     * @param fhzDeviceType the fhzDeviceType to set
     */
    public void setFhzDeviceType(FhzDeviceTypes fhzDeviceType) {
        this.fhzDeviceType = fhzDeviceType;
    }

    /**
     * @return the fhzProperty
     */
    public FhzProperty getFhzProperty() {
        return fhzProperty;
    }

    /**
     * @param fhzProperty the fhzProperty to set
     */
    public void setFhzProperty(FhzProperty fhzProperty) {
        this.fhzProperty = fhzProperty;
    }

    public void setDeviceHousecodeStr(String deviceHousecode) {
        this.deviceHousecode = Fhz1000.parseHouseCode(deviceHousecode);
    }

    public String getDeviceHousecodeStr() {
        return Fhz1000.houseCodeToString(deviceHousecode);
    }

    public String getFhzDeviceTypeLabel() {
        return fhzDeviceType.getLabel();
    }

    public void setFhzDeviceTypeLabel(String label) {
        fhzDeviceTypeLabel = label;
        tryFromDwr();
    }

    public void setFhzPropertyLabel(String label) {
        fhzPropertyLabel = label;
        tryFromDwr();
    }

    public String getFhzPropertyLabel() {
        return fhzProperty.getLabel();
    }

    private void tryFromDwr() {
        if ((fhzPropertyLabel != null) && (fhzDeviceTypeLabel != null)) {
            fhzDeviceType = fhzDeviceType.fromLabel(fhzDeviceTypeLabel);
            fhzProperty = FhzProperty.Util.fromLabel(fhzDeviceType, fhzPropertyLabel);
            fhzDeviceTypeLabel = null;
            fhzPropertyLabel = null;
        }
    }
}
