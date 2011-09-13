/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo.dataSource.fhz4j;

import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.web.i18n.LocalizableMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.List;
import java.util.Map;
import net.sf.fhz4j.Fhz1000;
import net.sf.fhz4j.FhzProtocol;
import net.sf.fhz4j.hms.HmsDeviceType;
import net.sf.fhz4j.hms.HmsProperty;

/**
 *
 * @author aploese
 */
public class HmsPointLocator extends Fhz4JPointLocatorVO<HmsProperty> {

    private short housecode;
    private HmsDeviceType hmsDeviceType;
    

    /**
     * @return the housecode
     */
    public short getHousecode() {
        return housecode;
    }

    /**
     * @param housecode the housecode to set
     */
    public void setHousecode(short housecode) {
        this.housecode = housecode;
    }

    public void setDeviceHousecodeStr(String deviceHousecode) {
        this.housecode = Fhz1000.parseHouseCode(deviceHousecode);
    }

    public String getDeviceHousecodeStr() {
        return Fhz1000.houseCodeToString(housecode);
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", housecode);
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", hmsDeviceType);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        HmsPointLocator from = (HmsPointLocator)o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.housecode, housecode);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.hmsDeviceType, hmsDeviceType);
    }
    
    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int SERIAL_VERSION = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(SERIAL_VERSION);
        out.writeShort(housecode);
        out.writeObject(hmsDeviceType);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        switch (ver) {
            case 1:
                housecode = in.readShort();
                hmsDeviceType = (HmsDeviceType) in.readObject();
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
        map.put("deviceHousecode", housecode);
        map.put("hmsDeviceType", hmsDeviceType);
    }

    @Override
    public FhzProtocol getFhzProtocol() {
        return FhzProtocol.HMS;
    }

    /**
     * @return the hmsDeviceType
     */
    public HmsDeviceType getHmsDeviceType() {
        return hmsDeviceType;
    }

    /**
     * @param hmsDeviceType the hmsDeviceType to set
     */
    public void setHmsDeviceType(HmsDeviceType hmsDeviceType) {
        this.hmsDeviceType = hmsDeviceType;
    }
}
