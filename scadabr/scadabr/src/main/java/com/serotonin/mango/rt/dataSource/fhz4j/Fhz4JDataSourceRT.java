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
package com.serotonin.mango.rt.dataSource.fhz4j;

import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import net.sf.fhz4j.fht.FhtMeasuredTempMessage;
import net.sf.fhz4j.fht.FhtMessage;

import net.sf.fhz4j.hms.HmsMessage;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataSource.EventDataSource;
import com.serotonin.mango.view.text.AnalogRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.fhz4j.FhtPointLocator;
import java.util.HashMap;
import java.util.Set;
import net.sf.fhz4j.FhzDataListener;
import net.sf.fhz4j.FhzParser;
import net.sf.fhz4j.FhzWriter;
import com.serotonin.mango.vo.dataSource.fhz4j.Fhz4JDataSourceVO;
import com.serotonin.mango.vo.dataSource.fhz4j.HmsPointLocator;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import net.sf.fhz4j.Fhz1000;
import net.sf.fhz4j.FhzProtocol;
import net.sf.fhz4j.fht.FhtProperty;
import net.sf.fhz4j.hms.HmsProperty;
import net.sf.fhz4j.scada.ScadaProperty;
import net.sf.fhz4j.scada.ScadaPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO datatype NUMERIC_INT is missing TODO Starttime for timpepoints ???
 * 
 */
public class Fhz4JDataSourceRT extends EventDataSource implements FhzDataListener {

    private final static Logger LOG = LoggerFactory.getLogger(Fhz4JDataSourceRT.class);
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int POINT_READ_EXCEPTION_EVENT = 2;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 3;
    // private final long nextRescan = 0;
    private SerialPort sPort;
    private FhzParser parser;
    private FhzWriter writer;
    private Fhz4JDataSourceVO vo;
    private Map<Short, Map<HmsProperty, DataPointRT>> hmsPoints = new HashMap<Short, Map<HmsProperty, DataPointRT>>();
    private Map<Short, Map<FhtProperty, DataPointRT>> fhtPoints = new HashMap<Short, Map<FhtProperty, DataPointRT>>();

    public Fhz4JDataSourceRT(Fhz4JDataSourceVO vo) {
        super(vo);
        this.vo = vo;
    }

    @Override
    public void initialize() {
        parser = new FhzParser(this);
        try {
            sPort = FhzParser.openPort(vo.getCommPortId());
            parser.setInputStream(sPort.getInputStream());
            writer = new FhzWriter();
            writer.setOutputStream(sPort.getOutputStream());
            writer.initFhz(vo.getFhzHousecode());
            if (vo.isFhzMaster()) {
                writer.initFhtReporting(getFhtDeviceHousecodes());
            }
        } catch (NoSuchPortException ex) {
            LOG.error("Cant open port: " + vo.getCommPortId(), ex);
            throw new RuntimeException(ex);
        } catch (PortInUseException ex) {
            LOG.error("Cant open port: " + vo.getCommPortId(), ex);
            throw new RuntimeException(ex);
        } catch (UnsupportedCommOperationException ex) {
            LOG.error("Cant open port: " + vo.getCommPortId(), ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            LOG.error("Cant open port: " + vo.getCommPortId(), ex);
            throw new RuntimeException(ex);
        } catch (UnsatisfiedLinkError ex) {
            LOG.error("Cant open port: " + vo.getCommPortId(), ex);
        }
        super.initialize();
    }

    @Override
    public void terminate() {
        try {
            parser.close();
            if (sPort != null) {
                sPort.close();
            }
        } catch (InterruptedException ex) {
            LOG.error(String.format("FHZ serialport: %s unexpected closed", sPort.getName()), ex);
        }
        super.terminate();
    }

    @Override
    public void addDataPoint(DataPointRT dataPoint) {
        synchronized (pointListChangeLock) {
            final Fhz4JPointLocatorRT locator = (Fhz4JPointLocatorRT) dataPoint.getPointLocator();
            switch (locator.getFhzProtocol()) {
                case FHT:
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getVo();
                    Map<FhtProperty, DataPointRT> fhzPropertyMap = fhtPoints.get(locator.getFhtHousecode());
                    if (fhzPropertyMap == null) {
                        fhzPropertyMap = new EnumMap<FhtProperty, DataPointRT>(FhtProperty.class);
                        fhtPoints.put(fhtLocator.getHousecode(), fhzPropertyMap);
                    }
                    fhzPropertyMap.put(fhtLocator.getProperty(), dataPoint);
                    break;
                case HMS:
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getVo();
                    Map<HmsProperty, DataPointRT> hmsPropertyMap = hmsPoints.get(locator.getHmsHousecode());
                    if (hmsPropertyMap == null) {
                        hmsPropertyMap = new EnumMap<HmsProperty, DataPointRT>(HmsProperty.class);
                        hmsPoints.put(hmsLocator.getHousecode(), hmsPropertyMap);
                    }
                    hmsPropertyMap.put(hmsLocator.getProperty(), dataPoint);
                    break;
                default:
                    throw new RuntimeException("Unknown fhz protocol");
            }
        }
    }

    @Override
    public void removeDataPoint(DataPointRT dataPoint) {
        synchronized (pointListChangeLock) {
            final Fhz4JPointLocatorRT locator = (Fhz4JPointLocatorRT) dataPoint.getPointLocator();
            switch (locator.getFhzProtocol()) {
                case FHT:
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getVo();
                    Map<FhtProperty, DataPointRT> fhtPropertyMap = fhtPoints.get(locator.getFhtHousecode());
                    fhtPropertyMap.remove(fhtLocator.getProperty());
                    if (fhtPropertyMap.isEmpty()) {
                        fhtPoints.remove(fhtLocator.getHousecode());
                    }
                    break;
                case HMS:
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getVo();
                    Map<HmsProperty, DataPointRT> hmsPropertyMap = hmsPoints.get(locator.getHmsHousecode());
                    hmsPropertyMap.remove(hmsLocator.getProperty());
                    if (hmsPropertyMap.isEmpty()) {
                        hmsPoints.remove(hmsLocator.getHousecode());
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown fhz protocol");
            }
        }
    }

    @Override
    public synchronized void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
    }

    public void addFoundFhtMessage(FhtMessage fhtMessage) {
        DataPointDao dataPointDao = new DataPointDao();

        // this value was not found, so create one
        FhtPointLocator fhtLocator = (FhtPointLocator) vo.createPontLocator(FhzProtocol.FHT);
        fhtLocator.setHousecode(fhtMessage.getHousecode());
        fhtLocator.setFhtDeviceType(fhtMessage.getCommand().getSupportedBy()[0]);
        fhtLocator.setProperty(fhtMessage.getCommand());
        fhtLocator.setSettable(false);

        DataPointVO dp = new DataPointVO();
        dp.setXid(dataPointDao.generateUniqueXid());
        dp.setName(fhtLocator.defaultName());
        dp.setDataSourceId(vo.getId());
        dp.setEnabled(true);
        dp.setLoggingType(DataPointVO.LoggingTypes.ON_CHANGE);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());


        dp.setPointLocator(fhtLocator);
        if (dp.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", fhtMessage.getCommand().getUnitOfMeasurement()));
        }

        DataPointRT dataPointRT = Common.ctx.getRuntimeManager().saveDataPoint(dp);
        Common.ctx.getRuntimeManager().addPointToHierarchy(dp, vo.getName(), Fhz1000.houseCodeToString(fhtMessage.getHousecode()) + " FHT" );

        if (dataPointRT != null) {
            updateValue(dataPointRT, fhtMessage, fhtMessage.getCommand());
        }
        LOG.error("FHT point added: " + dp.getXid());
    }

    private short[] getFhtDeviceHousecodes() {
        Set<Short> housecodes = fhtPoints.keySet();
        short[] result = new short[housecodes.size()];
        int i = 0;
        for (short housecode : housecodes) {
            result[i++] = housecode;
        }
        return result;
    }

    @Override
    public void fhtDataParsed(FhtMessage fhtMessage) {
        try {
            DataPointRT dataPoint = getFhtPoint(fhtMessage);
            if (dataPoint != null) {
                updateValue(dataPoint, fhtMessage, fhtMessage.getCommand());
            } else {
                LOG.error("NEW Fht property detected, wil add: " + fhtMessage);
                addFoundFhtMessage(fhtMessage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Unknown exception in fhtDataParsed", ex);
        }
    }

    @Override
    public void fhtMeasuredTempData(FhtMeasuredTempMessage temp) {
//TODO        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void hmsDataParsed(HmsMessage hmsMessage) {
        try {
            Map<HmsProperty, DataPointRT> dataPoints = getHmsPoints(hmsMessage);
            for (HmsProperty prop : hmsMessage.getDeviceType().getProperties()) {
                DataPointRT dataPoint = dataPoints == null ? null : dataPoints.get(prop);
                if (dataPoint == null) {
                    addFoundHmsDataPoint(hmsMessage, prop);
                } else {
                    updateValue(dataPoint, hmsMessage, prop);
                }
            }
        } catch (Exception ex) {
            System.err.print("Unknown exception in hmsDataParsed");
            ex.printStackTrace();
            LOG.error("Unknown exception in hmsDataParsed", ex);
        }
    }

    private void updateValue(DataPointRT point, ScadaPropertyProvider propertyProvider, ScadaProperty prop) {
        final long time = System.currentTimeMillis();
        switch (prop.getDataType()) {
            case BOOLEAN:
                point.updatePointValue(new PointValueTime(propertyProvider.getBoolean(prop), time));
                break;
            case BYTE:
                point.updatePointValue(new PointValueTime(propertyProvider.getByte(prop), time));
                break;
            case CHAR:
                point.updatePointValue(new PointValueTime(propertyProvider.getChar(prop), time));
                break;
            case DATE:
                point.updatePointValue(new PointValueTime(propertyProvider.asString(prop), time));
                break;
            case DOUBLE:
                point.updatePointValue(new PointValueTime(propertyProvider.getDouble(prop), time));
                break;
            case FLOAT:
                point.updatePointValue(new PointValueTime(propertyProvider.getFloat(prop), time));
                break;
            case INT:
                point.updatePointValue(new PointValueTime(propertyProvider.getInt(prop), time));
                break;
            case LONG:
                point.updatePointValue(new PointValueTime(propertyProvider.getLong(prop), time));
                break;
            case SHORT:
                point.updatePointValue(new PointValueTime(propertyProvider.getShort(prop), time));
                break;
            case STRING:
                point.updatePointValue(new PointValueTime(propertyProvider.getString(prop), time));
                break;
            case TIME:
                point.updatePointValue(new PointValueTime(propertyProvider.getTime(prop).toString(), time));
                break;
            case TIME_STAMP:
                point.updatePointValue(new PointValueTime(propertyProvider.asString(prop), time));
                break;
            default:
                throw new RuntimeException();
                
        }
    }

    private void addFoundHmsDataPoint(HmsMessage hmsMessage, HmsProperty prop) {
        DataPointDao dataPointDao = new DataPointDao();

        // this value was not found, so create one
        HmsPointLocator hmsLocator = (HmsPointLocator) vo.createPontLocator(FhzProtocol.HMS);
        hmsLocator.setHousecode(hmsMessage.getHousecode());
        hmsLocator.setHmsDeviceType(hmsMessage.getDeviceType());
        hmsLocator.setProperty(prop);
        hmsLocator.setSettable(false);

        DataPointVO dp = new DataPointVO();
        dp.setXid(dataPointDao.generateUniqueXid());
        dp.setName(hmsLocator.defaultName());
        dp.setDataSourceId(vo.getId());
        dp.setEnabled(true);
        dp.setLoggingType(DataPointVO.LoggingTypes.ON_CHANGE);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());

        dp.setPointLocator(hmsLocator);
        if (dp.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", prop.getUnitOfMeasurement()));
        }

        DataPointRT dataPointRT = Common.ctx.getRuntimeManager().saveDataPoint(dp);
        Common.ctx.getRuntimeManager().addPointToHierarchy(dp, vo.getName(), Fhz1000.houseCodeToString(hmsMessage.getHousecode()) + " " + hmsMessage.getDeviceType().getLabel());
        
        if (dataPointRT != null) {
            updateValue(dataPointRT, hmsMessage, prop);
        }
        LOG.error("HMS point added: " + dp.getXid());
    }

    private DataPointRT getFhtPoint(FhtMessage fhtMessage) {
        Map<FhtProperty, DataPointRT> fhtPropertyMap = fhtPoints.get(fhtMessage.getHousecode());
        if (fhtPropertyMap == null) {
            return null;
        }
        return fhtPropertyMap.get(fhtMessage.getCommand());
    }

    private Map<HmsProperty, DataPointRT> getHmsPoints(HmsMessage hmsMessage) {
        return hmsPoints.get(hmsMessage.getHousecode());
    }
}
