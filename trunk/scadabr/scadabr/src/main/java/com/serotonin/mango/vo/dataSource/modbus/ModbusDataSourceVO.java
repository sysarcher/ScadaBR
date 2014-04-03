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
package com.serotonin.mango.vo.dataSource.modbus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataSource.modbus.ModbusDataSource;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.modbus4j.Modbus;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;

/**
 * @author Matthew Lohbihler
 */
abstract public class ModbusDataSourceVO<T extends ModbusDataSourceVO<?>> extends DataSourceVO<T> {

    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
        ets.add(createEventType(ModbusDataSource.DATA_SOURCE_EXCEPTION_EVENT, new LocalizableMessageImpl(
                "event.ds.dataSource")));
        ets.add(createEventType(ModbusDataSource.POINT_READ_EXCEPTION_EVENT, new LocalizableMessageImpl(
                "event.ds.pointRead")));
        ets.add(createEventType(ModbusDataSource.POINT_WRITE_EXCEPTION_EVENT, new LocalizableMessageImpl(
                "event.ds.pointWrite")));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(ModbusDataSource.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(ModbusDataSource.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(ModbusDataSource.POINT_WRITE_EXCEPTION_EVENT, "POINT_WRITE_EXCEPTION");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public PointLocatorVO createPointLocator() {
        return new ModbusPointLocatorVO();
    }

    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonRemoteProperty
    private int updatePeriods = 5;
    @JsonRemoteProperty
    private boolean quantize;
    @JsonRemoteProperty
    private int timeout = 500;
    @JsonRemoteProperty
    private int retries = 2;
    @JsonRemoteProperty
    private boolean contiguousBatches;
    @JsonRemoteProperty
    private boolean createSlaveMonitorPoints;
    @JsonRemoteProperty
    private int maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
    @JsonRemoteProperty
    private int maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
    @JsonRemoteProperty
    private int maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    public int getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    public boolean isQuantize() {
        return quantize;
    }

    public void setQuantize(boolean quantize) {
        this.quantize = quantize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean isContiguousBatches() {
        return contiguousBatches;
    }

    public void setContiguousBatches(boolean contiguousBatches) {
        this.contiguousBatches = contiguousBatches;
    }

    public boolean isCreateSlaveMonitorPoints() {
        return createSlaveMonitorPoints;
    }

    public void setCreateSlaveMonitorPoints(boolean createSlaveMonitorPoints) {
        this.createSlaveMonitorPoints = createSlaveMonitorPoints;
    }

    public int getMaxReadBitCount() {
        return maxReadBitCount;
    }

    public void setMaxReadBitCount(int maxReadBitCount) {
        this.maxReadBitCount = maxReadBitCount;
    }

    public int getMaxReadRegisterCount() {
        return maxReadRegisterCount;
    }

    public void setMaxReadRegisterCount(int maxReadRegisterCount) {
        this.maxReadRegisterCount = maxReadRegisterCount;
    }

    public int getMaxWriteRegisterCount() {
        return maxWriteRegisterCount;
    }

    public void setMaxWriteRegisterCount(int maxWriteRegisterCount) {
        this.maxWriteRegisterCount = maxWriteRegisterCount;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType)) {
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        }
        if (updatePeriods <= 0) {
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        }
        if (timeout <= 0) {
            response.addContextualMessage("timeout", "validate.greaterThanZero");
        }
        if (retries < 0) {
            response.addContextualMessage("retries", "validate.cannotBeNegative");
        }
        if (maxReadBitCount < 1) {
            response.addContextualMessage("maxReadBitCount", "validate.greaterThanZero");
        }
        if (maxReadRegisterCount < 1) {
            response.addContextualMessage("maxReadRegisterCount", "validate.greaterThanZero");
        }
        if (maxWriteRegisterCount < 1) {
            response.addContextualMessage("maxWriteRegisterCount", "validate.greaterThanZero");
        }
    }

    @Override
    protected void addPropertiesImpl(List<LocalizableMessage> list) {
        AuditEventType.addPeriodMessage(list, "dsEdit.updatePeriod", updatePeriodType, updatePeriods);
        AuditEventType.addPropertyMessage(list, "dsEdit.quantize", quantize);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.timeout", timeout);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.retries", retries);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.contiguousBatches", contiguousBatches);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.createSlaveMonitorPoints", createSlaveMonitorPoints);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.maxReadBitCount", maxReadBitCount);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.maxReadRegisterCount", maxReadRegisterCount);
        AuditEventType.addPropertyMessage(list, "dsEdit.modbus.maxWriteRegisterCount", maxWriteRegisterCount);
    }

    @Override
    protected void addPropertyChangesImpl(List<LocalizableMessage> list, T from) {
        final ModbusDataSourceVO fromVO = (ModbusDataSourceVO) from;
        AuditEventType.maybeAddPeriodChangeMessage(list, "dsEdit.updatePeriod", fromVO.updatePeriodType,
                fromVO.updatePeriods, updatePeriodType, updatePeriods);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.quantize", fromVO.quantize, quantize);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.timeout", fromVO.timeout, timeout);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.retries", fromVO.retries, retries);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.contiguousBatches", fromVO.contiguousBatches,
                contiguousBatches);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.createSlaveMonitorPoints",
                fromVO.createSlaveMonitorPoints, createSlaveMonitorPoints);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.maxReadBitCount", fromVO.maxReadBitCount,
                maxReadBitCount);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.maxReadRegisterCount",
                fromVO.maxReadRegisterCount, maxReadRegisterCount);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.modbus.maxWriteRegisterCount",
                fromVO.maxWriteRegisterCount, maxWriteRegisterCount);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 7;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeBoolean(quantize);
        out.writeInt(timeout);
        out.writeInt(retries);
        out.writeBoolean(contiguousBatches);
        out.writeBoolean(createSlaveMonitorPoints);
        out.writeInt(maxReadBitCount);
        out.writeInt(maxReadRegisterCount);
        out.writeInt(maxWriteRegisterCount);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            updatePeriodType = Common.TimePeriods.SECONDS;
            updatePeriods = in.readInt();
            timeout = 500;
            retries = 2;
            contiguousBatches = false;
            createSlaveMonitorPoints = false;
            maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
            maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
            maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;
        } else if (ver == 2) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            timeout = 500;
            retries = 2;
            contiguousBatches = false;
            createSlaveMonitorPoints = false;
            maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
            maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
            maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;
        } else if (ver == 3) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            timeout = in.readInt();
            retries = in.readInt();
            contiguousBatches = false;
            createSlaveMonitorPoints = false;
            maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
            maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
            maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;
        } else if (ver == 4) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            timeout = in.readInt();
            retries = in.readInt();
            contiguousBatches = in.readBoolean();
            createSlaveMonitorPoints = false;
            maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
            maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
            maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;
        } else if (ver == 5) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            timeout = in.readInt();
            retries = in.readInt();
            contiguousBatches = in.readBoolean();
            createSlaveMonitorPoints = in.readBoolean();
            maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
            maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
            maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;
        } else if (ver == 6) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            quantize = in.readBoolean();
            timeout = in.readInt();
            retries = in.readInt();
            contiguousBatches = in.readBoolean();
            createSlaveMonitorPoints = in.readBoolean();
            maxReadBitCount = Modbus.DEFAULT_MAX_READ_BIT_COUNT;
            maxReadRegisterCount = Modbus.DEFAULT_MAX_READ_REGISTER_COUNT;
            maxWriteRegisterCount = Modbus.DEFAULT_MAX_WRITE_REGISTER_COUNT;
        } else if (ver == 7) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            quantize = in.readBoolean();
            timeout = in.readInt();
            retries = in.readInt();
            contiguousBatches = in.readBoolean();
            createSlaveMonitorPoints = in.readBoolean();
            maxReadBitCount = in.readInt();
            maxReadRegisterCount = in.readInt();
            maxWriteRegisterCount = in.readInt();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        Integer value = deserializeUpdatePeriodType(json);
        if (value != null) {
            updatePeriodType = value;
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        serializeUpdatePeriodType(map, updatePeriodType);
    }
}