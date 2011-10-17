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
package com.serotonin.mango.vo.dataSource.spinwave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.view.conversion.Conversions;
import com.serotonin.spinwave.SwMessage;
import com.serotonin.spinwave.v2.SensorValue;
import com.serotonin.spinwave.v2.SwMessageV2;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class SpinwaveV2PointLocatorVO extends BaseSpinwavePointLocatorVO {
    public final static Map<Integer, String> attributeTypes = new HashMap();
    static {
            attributeTypes.put(SensorValue.TYPE_TEMPERATURE, "dsEdit.spinwave.v2Attr.temp");
            attributeTypes.put(SensorValue.TYPE_SETPOINT, "dsEdit.spinwave.v2Attr.setPoint");
            attributeTypes.put(SensorValue.TYPE_BATTERY, "dsEdit.spinwave.v2Attr.battery");
            attributeTypes.put(SensorValue.TYPE_BATTERY_ALARM, "dsEdit.spinwave.v2Attr.batteryAlarm");
            attributeTypes.put(SensorValue.TYPE_OVERRIDE, "dsEdit.spinwave.v2Attr.override");
            attributeTypes.put(SensorValue.TYPE_HUMIDITY, "dsEdit.spinwave.v2Attr.humidity");
            attributeTypes.put(SensorValue.TYPE_VOLTAGE, "dsEdit.spinwave.v2Attr.voltage");
            attributeTypes.put(SensorValue.TYPE_AIRFLOW, "dsEdit.spinwave.v2Attr.airflow");
            attributeTypes.put(SensorValue.TYPE_KWH, "dsEdit.spinwave.v2Attr.kwhours");
            attributeTypes.put(SensorValue.TYPE_OCCUPANCY, "dsEdit.spinwave.v2Attr.occupancy");
            attributeTypes.put(SensorValue.TYPE_CO2, "dsEdit.spinwave.v2Attr.co2");
            attributeTypes.put(SensorValue.TYPE_VOC, "dsEdit.spinwave.v2Attr.voc");
            attributeTypes.put(SensorValue.TYPE_IAQ, "dsEdit.spinwave.v2Attr.iaq");
            attributeTypes.put(SensorValue.TYPE_CO, "dsEdit.spinwave.v2Attr.co");
            attributeTypes.put(SensorValue.TYPE_FREQUENCY, "dsEdit.spinwave.v2Attr.freq");
            attributeTypes.put(SensorValue.TYPE_PULSECOUNTER, "dsEdit.spinwave.v2Attr.counter");
    }

    public static MangoDataType getAttributeDataType(int attributeId) {
        if (attributeId == SensorValue.TYPE_BATTERY_ALARM || attributeId == SensorValue.TYPE_OVERRIDE
                || attributeId == SensorValue.TYPE_OCCUPANCY)
            return MangoDataType.BINARY;
        return MangoDataType.NUMERIC;
    }

    public static String getAttributeDescription(int attributeId) {
        for (Integer attr : attributeTypes.keySet()) {
            if (attributeId == attr)
                return attributeTypes.get(attr);
        }
        return "Unknown";
    }

    public Map<Integer, String> getAttributeTypes() {
        return attributeTypes;
    }

    @Override
    public String getAttributeDescription() {
        return getAttributeDescription(getAttributeId());
    }

    @Override
    public MangoDataType getMangoDataType() {
        return getAttributeDataType(getAttributeId());
    }

    @Override
    public MangoValue getValue(SwMessage msg) {
        SwMessageV2 message = (SwMessageV2) msg;
        SensorValue value = message.getValue(getAttributeId());

        if (value == null)
            return null;

        if (getAttributeDataType(getAttributeId()) == MangoDataType.BINARY)
            return new BinaryValue(value.getBinary());

        if (isConvertToCelsius())
            return new NumericValue(Conversions.fahrenheitToCelsius(value.getNumeric()));

        return new NumericValue(value.getNumeric());
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
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            // no op
        }
    }
}
