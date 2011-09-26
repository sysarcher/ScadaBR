/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo.dataSource.fhz4j;

import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.util.SerializationHelper;
import com.serotonin.web.i18n.LocalizableMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.fhz4j.FhzProtocol;
import net.sf.fhz4j.scada.ScadaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aploese
 */
public class ProtocolLocator<T extends ScadaProperty> implements Serializable, JsonSerializable {

    private final static Logger LOG = LoggerFactory.getLogger(FhtPointLocator.class);
    private T property;
    private boolean settable = false;

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

    void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", settable);
        AuditEventType.addPropertyMessage(list, "dsEdit.hfz4j.dataPoint", property);
    }

    void addPropertyChanges(List<LocalizableMessage> list, ProtocolLocator from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.isSettable(), settable);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.dataPoint", from.getProperty(), property);
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

    public FhzProtocol getFhzProtocol() {
        return FhzProtocol.UNKNOWN;
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
                property = (T) in.readObject();
                break;
            default:
                throw new RuntimeException("Cant handle version");
        }
    }

    /**
     * @return the settable
     */
    public boolean isSettable() {
        return settable;
    }

    /**
     * @param settable the settable to set
     */
    public void setSettable(boolean settable) {
        this.settable = settable;
    }
}
