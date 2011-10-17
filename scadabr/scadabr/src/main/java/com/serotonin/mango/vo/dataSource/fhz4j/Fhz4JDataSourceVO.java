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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.fhz4j.Fhz4JDataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceRegistry;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import net.sf.fhz4j.Fhz1000;
import net.sf.fhz4j.FhzProtocol;
import net.sf.fhz4j.fht.FhtDeviceTypes;

@JsonRemoteEntity
public class Fhz4JDataSourceVO extends DataSourceVO<Fhz4JDataSourceVO> {

    private final static int MAX_FHZ_ADDR = 9999;
    private final static Logger LOG = LoggerFactory.getLogger(Fhz4JDataSourceVO.class);
    private static final ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(Fhz4JDataSourceRT.SERIAL_PORT_EXCEPTION_EVENT, "SERIAL_PORT_EXCEPTION_EVENT");
        EVENT_CODES.addElement(Fhz4JDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(Fhz4JDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, "POINT_WRITE_EXCEPTION");
    }
    
    @JsonRemoteProperty
    private String commPortId;
    @JsonRemoteProperty
    private short fhzHousecode;
    @JsonRemoteProperty
    private boolean fhzMaster;
    

    @Override
    public DataSourceRegistry getType() {
        return DataSourceRegistry.FHZ_4_J;
    }

    @Override
    protected void addEventTypes(List<EventTypeVO> eventTypes) {
        eventTypes.add(createEventType(Fhz4JDataSourceRT.SERIAL_PORT_EXCEPTION_EVENT, new LocalizableMessage(
                "event.ds.serialPort")));
        eventTypes.add(createEventType(Fhz4JDataSourceRT.POINT_READ_EXCEPTION_EVENT, new LocalizableMessage(
                "event.ds.pointRead")));
        eventTypes.add(createEventType(Fhz4JDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, new LocalizableMessage(
                "event.ds.pointWrite")));
    }

    @Override
    public LocalizableMessage getConnectionDescription() {
        return new LocalizableMessage("common.default", commPortId);
    }

    @Override
    public PointLocatorVO createPointLocator() {
        return new Fhz4JPointLocatorVO();
    }
    
    public Fhz4JPointLocatorVO createPontLocator(FhzProtocol fhzProtocol) {
        switch (fhzProtocol) {
            case FHT:
                return new Fhz4JPointLocatorVO(new FhtPointLocator());
            case HMS:
                return new Fhz4JPointLocatorVO(new HmsPointLocator());
            case FHT_TEMP:
                return new Fhz4JPointLocatorVO(new FhtMeasuredTempPointLocator());
            default:
                throw new RuntimeException("Unknown protocol");
        }
    }
    


    @Override
    public DataSourceRT createDataSourceRT() {
        LOG.error("FHZ DS RT created");
        return new Fhz4JDataSourceRT(this);
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    protected void addPropertiesImpl(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.fhz4j.port", commPortId);
        AuditEventType.addPropertyMessage(list, "dsEdit.fhz4j.fhzHousecode", fhzHousecode);
        AuditEventType.addPropertyMessage(list, "dsEdit.fhz4j.fhzMaster", fhzMaster);
    }

    @Override
    protected void addPropertyChangesImpl(List<LocalizableMessage> list, Fhz4JDataSourceVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.port", from.commPortId, commPortId);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.fhzHousecode", from.fhzHousecode, fhzHousecode);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.fhz4j.fhzMaster", from.fhzMaster, fhzMaster);
    }

    public String getCommPortId() {
        return commPortId;
    }

    public void setCommPortId(String commPortId) {
        this.commPortId = commPortId;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);

        if (StringUtils.isEmpty(commPortId)) {
            response.addContextualMessage("commPortId", "validate.required");
        }
    }
    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int SERIAL_VERSION = 1;

    // Serialization for saveDataSource
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(SERIAL_VERSION);
        out.writeUTF(commPortId);
        out.writeShort(fhzHousecode);
        out.writeBoolean(fhzMaster);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        switch (ver) {
            case 1:
                commPortId = in.readUTF();
                fhzHousecode = in.readShort();
                fhzMaster = in.readBoolean();
                break;
            default:
                throw new RuntimeException("Cant read object from stream");
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

    /**
     * @return the fhzHousecode
     */
    public short getFhzHousecode() {
        return fhzHousecode;
    }

    /**
     * @return the fhzHousecode as String
     */
    public String getFhzHousecodeStr() {
        return Fhz1000.houseCodeToString(fhzHousecode);
    }


    /**
     * @param fhzHousecode the housecode ot this FHZ to set
     */
    public void setFhzHousecode(short fhzHousecode) {
        this.fhzHousecode = fhzHousecode;
    }

    /**
     * @return the fhzMaster
     */
    public boolean isFhzMaster() {
        return fhzMaster;
    }

    /**
     * @param fhzMaster the fhzMaster to set
     */
    public void setFhzMaster(boolean fhzMaster) {
        this.fhzMaster = fhzMaster;
    }

    public FhtDeviceTypes[] getDeviceTypes() {
        return FhtDeviceTypes.values();
    }

    public void setFhzHousecode(String fhzHousecode) {
        this.fhzHousecode = Fhz1000.parseHouseCode(fhzHousecode);
    }

}
