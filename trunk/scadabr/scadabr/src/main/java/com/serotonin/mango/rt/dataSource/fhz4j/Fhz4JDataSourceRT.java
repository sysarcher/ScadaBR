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

import gnu.io.SerialPort;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sf.fhz4j.Fhz1000;
import net.sf.fhz4j.FhzDataListener;
import net.sf.fhz4j.FhzParser;
import net.sf.fhz4j.FhzProtocol;
import net.sf.fhz4j.FhzWriter;
import net.sf.fhz4j.fht.FhtMessage;
import net.sf.fhz4j.fht.FhtProperty;
import net.sf.fhz4j.fht.FhtTempMessage;
import net.sf.fhz4j.fht.FhtTempPropery;
import net.sf.fhz4j.hms.HmsMessage;
import net.sf.fhz4j.hms.HmsProperty;
import net.sf.fhz4j.scada.ScadaProperty;
import net.sf.fhz4j.scada.ScadaPropertyProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.view.text.AnalogRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.fhz4j.FhtMeasuredTempPointLocator;
import com.serotonin.mango.vo.dataSource.fhz4j.FhtPointLocator;
import com.serotonin.mango.vo.dataSource.fhz4j.Fhz4JDataSourceVO;
import com.serotonin.mango.vo.dataSource.fhz4j.Fhz4JPointLocatorVO;
import com.serotonin.mango.vo.dataSource.fhz4j.HmsPointLocator;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 *
 * TODO datatype NUMERIC_INT is missing TODO Starttime for timpepoints ???
 *
 */
public class Fhz4JDataSourceRT extends DataSourceRT implements FhzDataListener {

    private final static Logger LOG = LoggerFactory.getLogger(Fhz4JDataSourceRT.class);
    public static final int SERIAL_PORT_EXCEPTION_EVENT = 1;
    public static final int POINT_READ_EXCEPTION_EVENT = 2;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 3;
    // private final long nextRescan = 0;
    private SerialPort sPort;
    private FhzParser parser;
    private FhzWriter writer;
    private Fhz4JDataSourceVO vo;
    private final Map<Short, Map<HmsProperty, DataPointRT>> hmsPoints = new HashMap<Short, Map<HmsProperty, DataPointRT>>();
    private final Map<Short, Map<FhtProperty, DataPointRT>> fhtPoints = new HashMap<Short, Map<FhtProperty, DataPointRT>>();
    private final Map<Short, DataPointRT> fhtTempPoints = new HashMap<Short, DataPointRT>();
    private final Map<Short, Map<HmsProperty, DataPointVO>> hmsDisabledPoints = new HashMap<Short, Map<HmsProperty, DataPointVO>>();
    private final Map<Short, Map<FhtProperty, DataPointVO>> fhtDisabledPoints = new HashMap<Short, Map<FhtProperty, DataPointVO>>();
    private final Map<Short, DataPointVO> fhtTempDisabledPoints = new HashMap<Short, DataPointVO>();
    @Autowired
    private RuntimeManager runtimeManager;

    public Fhz4JDataSourceRT(Fhz4JDataSourceVO vo) {
        super(vo, true);
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
            returnToNormal(SERIAL_PORT_EXCEPTION_EVENT, System.currentTimeMillis());
        } catch (Exception ex) {
            raiseEvent(SERIAL_PORT_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    new LocalizableMessage("event.exception", vo.getName(), ex.getMessage()));
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
            raiseEvent(SERIAL_PORT_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    new LocalizableMessage("event.exception", vo.getName(), ex.getMessage()));
            LOG.error(String.format("FHZ serialport: %s unexpected closed", sPort.getName()), ex);
        }
        super.terminate();
    }

    @Override
    public void dataPointEnabled(DataPointRT dataPoint) {
        addToEnabledDataPoint(dataPoint);
        removeFromDisabledDataPoint(dataPoint.getVO());
    }

    @Override
    public void dataPointDisabled(DataPointRT dataPoint) {
        removeFromEnabledDataPoint(dataPoint);
        addToDisabledDataPoint(dataPoint.getVO());
    }

    @Override
    public void dataPointDeleted(DataPointVO point) {
        removeFromDisabledDataPoint(point);
    }

    private void addToEnabledDataPoint(DataPointRT dataPoint) {
        final Fhz4JPointLocatorRT locator = (Fhz4JPointLocatorRT) dataPoint.getPointLocator();
        switch (locator.getFhzProtocol()) {
            case FHT:
                synchronized (fhtPoints) {
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getVo().getProtocolLocator();
                    Map<FhtProperty, DataPointRT> fhzPropertyMap = fhtPoints.get(fhtLocator.getHousecode());
                    if (fhzPropertyMap == null) {
                        fhzPropertyMap = new EnumMap<FhtProperty, DataPointRT>(FhtProperty.class);
                        fhtPoints.put(fhtLocator.getHousecode(), fhzPropertyMap);
                    }
                    fhzPropertyMap.put(fhtLocator.getProperty(), dataPoint);
                }
                break;
            case HMS:
                synchronized (hmsPoints) {
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getVo().getProtocolLocator();
                    Map<HmsProperty, DataPointRT> hmsPropertyMap = hmsPoints.get(hmsLocator.getHousecode());
                    if (hmsPropertyMap == null) {
                        hmsPropertyMap = new EnumMap<HmsProperty, DataPointRT>(HmsProperty.class);
                        hmsPoints.put(hmsLocator.getHousecode(), hmsPropertyMap);
                    }
                    hmsPropertyMap.put(hmsLocator.getProperty(), dataPoint);
                }
                break;
            case FHT_TEMP:
                synchronized (fhtTempPoints) {
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getVo().getProtocolLocator();
                    fhtTempPoints.put(fhtTempLocator.getHousecode(), dataPoint);
                }
                break;
            default:
                throw new ShouldNeverHappenException("Unknown fhz protocol");
        }
    }

    /**
     * let the runtime know of all disabled DataSources
     * @param vo
     */
    public void addToDisabledDataPoint(DataPointVO vo) {
        super.addDisabledDataPoint(vo);
        final Fhz4JPointLocatorVO locator = (Fhz4JPointLocatorVO) vo.getPointLocator();
        switch (locator.getFhzProtocol()) {
            case FHT:
                synchronized (fhtDisabledPoints) {
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getProtocolLocator();
                    Map<FhtProperty, DataPointVO> fhzPropertyMap = fhtDisabledPoints.get(fhtLocator.getHousecode());
                    if (fhzPropertyMap == null) {
                        fhzPropertyMap = new EnumMap<FhtProperty, DataPointVO>(FhtProperty.class);
                        fhtDisabledPoints.put(fhtLocator.getHousecode(), fhzPropertyMap);
                    }
                    fhzPropertyMap.put(fhtLocator.getProperty(), vo);
                }
                break;
            case HMS:
                synchronized (hmsDisabledPoints) {
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getProtocolLocator();
                    Map<HmsProperty, DataPointVO> hmsPropertyMap = hmsDisabledPoints.get(hmsLocator.getHousecode());
                    if (hmsPropertyMap == null) {
                        hmsPropertyMap = new EnumMap<HmsProperty, DataPointVO>(HmsProperty.class);
                        hmsDisabledPoints.put(hmsLocator.getHousecode(), hmsPropertyMap);
                    }
                    hmsPropertyMap.put(hmsLocator.getProperty(), vo);
                }
                break;
            case FHT_TEMP:
                synchronized (fhtTempDisabledPoints) {
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getProtocolLocator();
                    fhtTempDisabledPoints.put(fhtTempLocator.getHousecode(), vo);
                }
                break;
            default:
                throw new ShouldNeverHappenException("Unknown fhz protocol");
        }
    }

    public void removeFromEnabledDataPoint(DataPointRT dataPoint) {
        addDisabledDataPoint(dataPoint.getVO());
        final Fhz4JPointLocatorRT locator = (Fhz4JPointLocatorRT) dataPoint.getPointLocator();
        switch (locator.getFhzProtocol()) {
            case FHT:
                synchronized (fhtPoints) {
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getVo().getProtocolLocator();
                    Map<FhtProperty, DataPointRT> fhtPropertyMap = fhtPoints.get(fhtLocator.getHousecode());
                    fhtPropertyMap.remove(fhtLocator.getProperty());
                    if (fhtPropertyMap.isEmpty()) {
                        fhtPoints.remove(fhtLocator.getHousecode());
                    }
                }
                break;
            case HMS:
                synchronized (hmsPoints) {
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getVo().getProtocolLocator();
                    Map<HmsProperty, DataPointRT> hmsPropertyMap = hmsPoints.get(hmsLocator.getHousecode());
                    hmsPropertyMap.remove(hmsLocator.getProperty());
                    if (hmsPropertyMap.isEmpty()) {
                        hmsPoints.remove(hmsLocator.getHousecode());
                    }
                }
                break;
            case FHT_TEMP:
                synchronized (fhtTempPoints) {
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getVo().getProtocolLocator();
                    fhtTempPoints.remove(fhtTempLocator.getHousecode());
                }
                break;
            default:
                throw new ShouldNeverHappenException("Unknown fhz protocol");
        }
    }

    private void removeFromDisabledDataPoint(DataPointVO vo) {
        final Fhz4JPointLocatorVO locator = (Fhz4JPointLocatorVO) vo.getPointLocator();
        switch (locator.getFhzProtocol()) {
            case FHT:
                synchronized (fhtDisabledPoints) {
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getProtocolLocator();
                    Map<FhtProperty, DataPointVO> fhtPropertyMap = fhtDisabledPoints.get(fhtLocator.getHousecode());
                    if (fhtPropertyMap == null) {
                        return;
                    }
                    fhtPropertyMap.remove(fhtLocator.getProperty());
                    if (fhtPropertyMap.isEmpty()) {
                        fhtDisabledPoints.remove(fhtLocator.getHousecode());
                    }
                }
                break;
            case HMS:
                synchronized (hmsDisabledPoints) {

                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getProtocolLocator();
                    Map<HmsProperty, DataPointVO> hmsPropertyMap = hmsDisabledPoints.get(hmsLocator.getHousecode());
                    if (hmsPropertyMap == null) {
                        return;
                    }
                    hmsPropertyMap.remove(hmsLocator.getProperty());
                    if (hmsPropertyMap.isEmpty()) {
                        hmsDisabledPoints.remove(hmsLocator.getHousecode());
                    }
                }
                break;
            case FHT_TEMP:
                synchronized (fhtTempDisabledPoints) {
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getProtocolLocator();
                    fhtTempDisabledPoints.remove(fhtTempLocator.getHousecode());
                }
                break;
            default:
                throw new ShouldNeverHappenException("Unknown fhz protocol");
        }
    }

    @Override
    public synchronized void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
    }

    public void addFoundFhtMessage(FhtMessage fhtMessage) {
        DataPointDao dataPointDao = new DataPointDao();

        // this value was not found, so create one
        final Fhz4JPointLocatorVO<FhtProperty> fhzLocator = (Fhz4JPointLocatorVO<FhtProperty>) vo.createPontLocator(FhzProtocol.FHT);
        final FhtPointLocator fhtLocator = (FhtPointLocator) fhzLocator.getProtocolLocator();
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


        dp.setPointLocator(fhzLocator);
        if (dp.getPointLocator().getMangoDataType() == MangoDataType.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", fhtMessage.getCommand().getUnitOfMeasurement()));
        }

        DataPointRT dataPointRT = runtimeManager.saveDataPoint(dp);
        runtimeManager.addPointToHierarchy(dp, vo.getName(), Fhz1000.houseCodeToString(fhtMessage.getHousecode()) + " FHT");

        if (dataPointRT != null) {
            updateValue(dataPointRT, fhtMessage, fhtMessage.getCommand());
        }
        LOG.info("FHT point added: " + dp.getXid());
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
        synchronized (fhtPoints) {
            synchronized (fhtDisabledPoints) {
                try {
                    DataPointRT dataPoint = getFhtPoint(fhtMessage);
                    if (dataPoint != null) {
                        updateValue(dataPoint, fhtMessage, fhtMessage.getCommand());
                    } else {
                        if (getFhtDisabledPoint(fhtMessage) == null) {
                            LOG.error("NEW Fht property detected, wil add: " + fhtMessage);
                            addFoundFhtMessage(fhtMessage);
                        }
                    }
                    returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
                } catch (Exception ex) {
                    raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                            new LocalizableMessage("event.exception", vo.getName(), ex.getMessage()));
                }
            }
        }
    }

    @Override
    public void fhtCombinedData(FhtTempMessage fhtTempMessage) {
        synchronized (fhtTempPoints) {
            synchronized (fhtTempDisabledPoints) {
                try {
                    DataPointRT dataPoint = getFhtTempPoint(fhtTempMessage);
                    if (dataPoint != null) {
                        updateValue(dataPoint, fhtTempMessage, fhtTempMessage.getProperty());
                    } else {
                        if (getFhtTempDisabledPoint(fhtTempMessage) == null) {
                            LOG.error("NEW Fht property detected, wil add: " + fhtTempMessage);
                            addFoundFhtTempMessage(fhtTempMessage);
                        }
                    }
                    returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
                } catch (Exception ex) {
                    raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                            new LocalizableMessage("event.exception", vo.getName(), ex.getMessage()));
                }
            }
        }
    }

    @Override
    public void hmsDataParsed(HmsMessage hmsMessage) {
        synchronized (hmsPoints) {
            synchronized (hmsDisabledPoints) {
                try {
                    Map<HmsProperty, DataPointRT> hmsDataPoints = getHmsPoints(hmsMessage);
                    for (HmsProperty prop : hmsMessage.getDeviceType().getProperties()) {
                        DataPointRT dataPoint = hmsDataPoints == null ? null : hmsDataPoints.get(prop);
                        if (dataPoint == null) {
                            if (getHmsDisabledPoint(hmsMessage, prop) == null) {
                                addFoundHmsDataPoint(hmsMessage, prop);
                            }
                        } else {
                            updateValue(dataPoint, hmsMessage, prop);
                        }
                    }
                    returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
                } catch (Exception ex) {
                    raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                            new LocalizableMessage("event.exception", vo.getName(), ex.getMessage()));
                }
            }
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
        Fhz4JPointLocatorVO<HmsProperty> fhzLocator = (Fhz4JPointLocatorVO<HmsProperty>) vo.createPontLocator(FhzProtocol.HMS);
        HmsPointLocator hmsLocator = (HmsPointLocator) fhzLocator.getProtocolLocator();
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

        dp.setPointLocator(fhzLocator);
        if (dp.getPointLocator().getMangoDataType() == MangoDataType.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", prop.getUnitOfMeasurement()));
        }

        DataPointRT dataPointRT = runtimeManager.saveDataPoint(dp);
        runtimeManager.addPointToHierarchy(dp, vo.getName(), hmsLocator.getHousecodeStr() + " " + hmsMessage.getDeviceType().getLabel());

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
        final DataPointRT dp = fhtPropertyMap.get(fhtMessage.getCommand());
        return dp;
    }

    private DataPointVO getFhtDisabledPoint(FhtMessage fhtMessage) {
        Map<FhtProperty, DataPointVO> fhtPropertyMap = fhtDisabledPoints.get(fhtMessage.getHousecode());
        if (fhtPropertyMap == null) {
            return null;
        }
        final DataPointVO dp = fhtPropertyMap.get(fhtMessage.getCommand());
        return dp;
    }

    private Map<HmsProperty, DataPointRT> getHmsPoints(HmsMessage hmsMessage) {
        return hmsPoints.get(hmsMessage.getHousecode());
    }

    private Object getHmsDisabledPoint(HmsMessage hmsMessage, HmsProperty hmsProperty) {
        Map<HmsProperty, DataPointVO> hmsPropertyMap = hmsDisabledPoints.get(hmsMessage.getHousecode());
        if (hmsPropertyMap == null) {
            return null;
        }
        final DataPointVO dp = hmsPropertyMap.get(hmsProperty);
        return dp;
    }

    private DataPointRT getFhtTempPoint(FhtTempMessage fhtTempMessage) {
        return fhtTempPoints.get(fhtTempMessage.getHousecode());
    }

    private Object getFhtTempDisabledPoint(FhtTempMessage fhtTempMessage) {
        return fhtTempDisabledPoints.get(fhtTempMessage.getHousecode());
    }

    private void addFoundFhtTempMessage(FhtTempMessage fhtTempMessage) {
        DataPointDao dataPointDao = new DataPointDao();

        // this value was not found, so create one
        final Fhz4JPointLocatorVO<FhtTempPropery> fhzLocator = (Fhz4JPointLocatorVO<FhtTempPropery>) vo.createPontLocator(FhzProtocol.FHT_TEMP);
        final FhtMeasuredTempPointLocator fhtLocator = (FhtMeasuredTempPointLocator) fhzLocator.getProtocolLocator();
        fhtLocator.setHousecode(fhtTempMessage.getHousecode());
        fhtLocator.setFhtDeviceType(fhtTempMessage.getCommand().getSupportedBy()[0]);
        fhtLocator.setProperty(fhtTempMessage.getProperty());
        fhtLocator.setSettable(false);

        DataPointVO dp = new DataPointVO();
        dp.setXid(dataPointDao.generateUniqueXid());
        dp.setName(fhtLocator.defaultName());
        dp.setDataSourceId(vo.getId());
        dp.setEnabled(true);
        dp.setLoggingType(DataPointVO.LoggingTypes.ON_CHANGE);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());


        dp.setPointLocator(fhzLocator);
        if (dp.getPointLocator().getMangoDataType() == MangoDataType.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", fhtTempMessage.getProperty().getUnitOfMeasurement()));
        }

        DataPointRT dataPointRT = runtimeManager.saveDataPoint(dp);
        runtimeManager.addPointToHierarchy(dp, vo.getName(), Fhz1000.houseCodeToString(fhtTempMessage.getHousecode()) + " FHT");

        if (dataPointRT != null) {
            updateValue(dataPointRT, fhtTempMessage, fhtTempMessage.getProperty());
        }
        LOG.info("FHT point added: " + dp.getXid());
    }
}
