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
package br.org.scadabr.rt.datasource.fhz4j;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.vo.datasource.fhz4j.FhtMeasuredTempPointLocator;
import br.org.scadabr.vo.datasource.fhz4j.FhtPointLocator;
import br.org.scadabr.vo.datasource.fhz4j.Fhz4JDataSourceVO;
import br.org.scadabr.vo.datasource.fhz4j.Fhz4JPointLocatorVO;
import br.org.scadabr.vo.datasource.fhz4j.HmsPointLocator;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.sf.fhz4j.Fhz1000;
import net.sf.fhz4j.FhzDataListener;
import net.sf.fhz4j.FhzParser;
import net.sf.fhz4j.FhzProtocol;
import net.sf.fhz4j.FhzWriter;
import net.sf.fhz4j.fht.FhtMessage;
import net.sf.fhz4j.fht.FhtProperty;
import net.sf.fhz4j.hms.HmsMessage;
import net.sf.fhz4j.hms.HmsProperty;
import net.sf.fhz4j.scada.ScadaProperty;
import net.sf.fhz4j.scada.ScadaPropertyProvider;

import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.view.chart.ImageChartRenderer;
import com.serotonin.mango.view.text.AnalogRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.sf.atmodem4j.spsw.SerialPortSocket;
import net.sf.fhz4j.fht.FhtTempMessage;
import net.sf.fhz4j.fht.FhtTempPropery;

/**
 *
 * TODO datatype NUMERIC_INT is missing TODO Starttime for timpepoints ???
 *
 */
public class Fhz4JDataSourceRT extends DataSourceRT<Fhz4JDataSourceVO> implements FhzDataListener {

    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCARABR_DS_FHZ4J);
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int POINT_READ_EXCEPTION_EVENT = 2;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 3;
    // private final long nextRescan = 0;
    private SerialPortSocket sPort;
    private FhzParser parser;
    private FhzWriter writer;
    private final Map<Short, Map<HmsProperty, DataPointRT>> hmsPoints = new HashMap();
    private final Map<Short, Map<FhtProperty, DataPointRT>> fhtPoints = new HashMap();
    private final Map<Short, DataPointRT> fhtTempPoints = new HashMap();
    private final Map<Short, Map<HmsProperty, DataPointVO>> hmsDisabledPoints = new HashMap();
    private final Map<Short, Map<FhtProperty, DataPointVO>> fhtDisabledPoints = new HashMap();
    private final Map<Short, DataPointVO> fhtTempDisabledPoints = new HashMap();

    public Fhz4JDataSourceRT(Fhz4JDataSourceVO vo) {
        super(vo, true);
    }

    @Override
    public void initialize() {
        parser = new FhzParser(this);
        try {
            sPort = FhzParser.openPort(vo.getCommPort());
            parser.setInputStream(sPort.getInputStream());
            writer = new FhzWriter();
            writer.setOutputStream(sPort.getOutputStream());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            writer.initFhz(vo.getFhzHousecode());
            if (vo.isFhzMaster()) {
                writer.initFhtReporting(getFhtDeviceHousecodes());
            }
            returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
        } catch (IOException ex) {
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    new LocalizableMessageImpl("event.exception2", vo.getName(), ex.getMessage()));
            final LogRecord lr = new LogRecord(Level.SEVERE, "FHZ open serialport: {0}");
            lr.setParameters(new Object[]{sPort.getPortName()});
            lr.setThrown(ex);
            LOG.log(lr);
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
        } catch (InterruptedException | IOException ex) {
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    new LocalizableMessageImpl("event.exception2", vo.getName(), ex.getMessage()));
            final LogRecord lr = new LogRecord(Level.SEVERE, "FHZ serialport: {0} unexpected closed");
            lr.setParameters(new Object[]{sPort.getPortName()});
            lr.setThrown(ex);
            LOG.log(lr);
        }
        super.terminate();
    }

    @Override
    public void dataPointEnabled(DataPointRT dataPoint) {
        addToEnabledDataPoint(dataPoint);
        removeFromDisabledDataPoint(dataPoint.getVo());
    }

    @Override
    public void dataPointDisabled(DataPointVO dataPoint) {
        removeFromEnabledDataPoint(dataPoint);
        addToDisabledDataPoint(dataPoint);
    }

    @Override
    public void dataPointDeleted(DataPointVO point) {
        removeFromDisabledDataPoint(point);
    }

    private void addToEnabledDataPoint(DataPointRT dataPoint) {
        synchronized (dataPointsCacheLock) {
            final Fhz4JPointLocatorRT locator = (Fhz4JPointLocatorRT) dataPoint.getPointLocator();
            switch (locator.getFhzProtocol()) {
                case FHT:
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getVo().getProtocolLocator();
                    Map<FhtProperty, DataPointRT> fhzPropertyMap = fhtPoints.get(fhtLocator.getHousecode());
                    if (fhzPropertyMap == null) {
                        fhzPropertyMap = new EnumMap<>(FhtProperty.class);
                        fhtPoints.put(fhtLocator.getHousecode(), fhzPropertyMap);
                    }
                    fhzPropertyMap.put(fhtLocator.getProperty(), dataPoint);
                    break;
                case HMS:
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getVo().getProtocolLocator();
                    Map<HmsProperty, DataPointRT> hmsPropertyMap = hmsPoints.get(hmsLocator.getHousecode());
                    if (hmsPropertyMap == null) {
                        hmsPropertyMap = new EnumMap<>(HmsProperty.class);
                        hmsPoints.put(hmsLocator.getHousecode(), hmsPropertyMap);
                    }
                    hmsPropertyMap.put(hmsLocator.getProperty(), dataPoint);
                    break;
                case FHT_TEMP:
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getVo().getProtocolLocator();
                    fhtTempPoints.put(fhtTempLocator.getHousecode(), dataPoint);
                    break;
                default:
                    throw new RuntimeException("Unknown fhz protocol");
            }
        }
    }

    /**
     * let the runtime know of all disabled DataSources
     *
     * @param vo
     */
    public void addToDisabledDataPoint(DataPointVO vo) {
        synchronized (dataPointsCacheLock) {
            final Fhz4JPointLocatorVO locator = (Fhz4JPointLocatorVO) vo.getPointLocator();
            switch (locator.getFhzProtocol()) {
                case FHT:
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getProtocolLocator();
                    Map<FhtProperty, DataPointVO> fhzPropertyMap = fhtDisabledPoints.get(fhtLocator.getHousecode());
                    if (fhzPropertyMap == null) {
                        fhzPropertyMap = new EnumMap<>(FhtProperty.class);
                        fhtDisabledPoints.put(fhtLocator.getHousecode(), fhzPropertyMap);
                    }
                    fhzPropertyMap.put(fhtLocator.getProperty(), vo);
                    break;
                case HMS:
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getProtocolLocator();
                    Map<HmsProperty, DataPointVO> hmsPropertyMap = hmsDisabledPoints.get(hmsLocator.getHousecode());
                    if (hmsPropertyMap == null) {
                        hmsPropertyMap = new EnumMap<>(HmsProperty.class);
                        hmsDisabledPoints.put(hmsLocator.getHousecode(), hmsPropertyMap);
                    }
                    hmsPropertyMap.put(hmsLocator.getProperty(), vo);
                    break;
                case FHT_TEMP:
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getProtocolLocator();
                    fhtTempDisabledPoints.put(fhtTempLocator.getHousecode(), vo);
                    break;
                default:
                    throw new RuntimeException("Unknown fhz protocol");
            }
        }
    }

    public void removeFromEnabledDataPoint(DataPointVO dataPoint) {
        synchronized (dataPointsCacheLock) {
            final Fhz4JPointLocatorVO locator = (Fhz4JPointLocatorVO) dataPoint.getPointLocator();
            switch (locator.getFhzProtocol()) {
                case FHT:
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getProtocolLocator();
                    Map<FhtProperty, DataPointRT> fhtPropertyMap = fhtPoints.get(fhtLocator.getHousecode());
                    fhtPropertyMap.remove(fhtLocator.getProperty());
                    if (fhtPropertyMap.isEmpty()) {
                        fhtPoints.remove(fhtLocator.getHousecode());
                    }
                    break;
                case HMS:
                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getProtocolLocator();
                    Map<HmsProperty, DataPointRT> hmsPropertyMap = hmsPoints.get(hmsLocator.getHousecode());
                    hmsPropertyMap.remove(hmsLocator.getProperty());
                    if (hmsPropertyMap.isEmpty()) {
                        hmsPoints.remove(hmsLocator.getHousecode());
                    }
                    break;
                case FHT_TEMP:
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getProtocolLocator();
                    fhtTempPoints.remove(fhtTempLocator.getHousecode());
                    break;
                default:
                    throw new ShouldNeverHappenException("Unknown fhz protocol");
            }
        }
    }

    private void removeFromDisabledDataPoint(DataPointVO vo) {
        synchronized (dataPointsCacheLock) {
            final Fhz4JPointLocatorVO locator = (Fhz4JPointLocatorVO) vo.getPointLocator();
            switch (locator.getFhzProtocol()) {
                case FHT:
                    FhtPointLocator fhtLocator = (FhtPointLocator) locator.getProtocolLocator();
                    Map<FhtProperty, DataPointVO> fhtPropertyMap = fhtDisabledPoints.get(fhtLocator.getHousecode());
                    if (fhtPropertyMap == null) {
                        return;
                    }
                    fhtPropertyMap.remove(fhtLocator.getProperty());
                    if (fhtPropertyMap.isEmpty()) {
                        fhtDisabledPoints.remove(fhtLocator.getHousecode());
                    }
                    break;
                case HMS:

                    HmsPointLocator hmsLocator = (HmsPointLocator) locator.getProtocolLocator();
                    Map<HmsProperty, DataPointVO> hmsPropertyMap = hmsDisabledPoints.get(hmsLocator.getHousecode());
                    if (hmsPropertyMap == null) {
                        return;
                    }
                    hmsPropertyMap.remove(hmsLocator.getProperty());
                    if (hmsPropertyMap.isEmpty()) {
                        hmsDisabledPoints.remove(hmsLocator.getHousecode());
                    }
                    break;
                case FHT_TEMP:
                    FhtMeasuredTempPointLocator fhtTempLocator = (FhtMeasuredTempPointLocator) locator.getProtocolLocator();
                    fhtTempDisabledPoints.remove(fhtTempLocator.getHousecode());
                    break;
                default:
                    throw new ShouldNeverHappenException("Unknown fhz protocol");
            }
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
        dp.setName(fhtLocator.defaultName());
        dp.setDataSourceId(vo.getId());
        dp.setXid(String.format("%04x-%s", fhtMessage.getHousecode(), fhtMessage.getCommand().getName()));
        dp.setPointLocator(fhzLocator);
        dp.setEnabled(true);
        dp.setLoggingType(DataPointVO.LoggingTypes.ALL);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());

        if (dp.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", fhtMessage.getCommand().getUnitOfMeasurement()));
            dp.setChartRenderer(new ImageChartRenderer(Common.TimePeriods.DAYS, 1));
        }

        DataPointRT dataPointRT = Common.ctx.getRuntimeManager().saveDataPoint(dp);
        Common.ctx.getRuntimeManager().addPointToHierarchy(dp, vo.getName(), Fhz1000.houseCodeToString(fhtMessage.getHousecode()) + " FHT");

        if (dataPointRT != null) {
            updateValue(dataPointRT, fhtMessage, fhtMessage.getCommand());
        }
        LOG.log(Level.INFO, "FHT point added: {0}", dp.getXid());
    }

    private Iterable<Short> getFhtDeviceHousecodes() {
        return fhtPoints.keySet();
    }

    @Override
    public void fhtDataParsed(FhtMessage fhtMessage) {
        LOG.log(Level.INFO, "FHT SignalStrength : {0} {1}", new Object[]{Fhz1000.houseCodeToString(fhtMessage.getHousecode()), fhtMessage.getSignalStrength()});
        if (fhtMessage.getCommand() == FhtProperty.MEASURED_LOW || fhtMessage.getCommand() == FhtProperty.MEASURED_HIGH) {
            // Just ignore this...
            return;
        }
        try {
            DataPointRT dataPoint = getFhtPoint(fhtMessage);
            if (dataPoint != null) {
                updateValue(dataPoint, fhtMessage, fhtMessage.getCommand());
            } else {
                if (getFhtDisabledPoint(fhtMessage) == null) {
                    LOG.log(Level.INFO, "NEW FHT property detected, wil add: {0}", fhtMessage);
                    addFoundFhtMessage(fhtMessage);
                }
            }
            returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
        } catch (Throwable t) {
            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new LocalizableMessageImpl("event.exception2", vo.getName(), t.getMessage()));
            final LogRecord lr = new LogRecord(Level.SEVERE, "FHZ hms parsed: {0}");
            lr.setParameters(new Object[]{fhtMessage});
            lr.setThrown(t);
            LOG.log(lr);
        }
    }

    @Override
    public void fhtCombinedData(FhtTempMessage fhtTempMessage) {
        try {
            DataPointRT dataPoint = getFhtTempPoint(fhtTempMessage);
            if (dataPoint != null) {
                updateValue(dataPoint, fhtTempMessage, fhtTempMessage.getProperty());
            } else {
                if (getFhtTempDisabledPoint(fhtTempMessage) == null) {
                    LOG.log(Level.SEVERE, "NEW Fht property detected, wil add: {0}", fhtTempMessage);
                    addFoundFhtTempMessage(fhtTempMessage);
                }
            }
            returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
        } catch (Throwable t) {
            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new LocalizableMessageImpl("event.exception2", vo.getName(), t.getMessage()));
            final LogRecord lr = new LogRecord(Level.SEVERE, "FHZ hms parsed: {0}");
            lr.setParameters(new Object[]{fhtTempMessage});
            lr.setThrown(t);
            LOG.log(lr);
        }
    }

    @Override
    public void hmsDataParsed(HmsMessage hmsMessage) {
        LOG.log(Level.INFO, "HMS SignalStrength : {0} {1}", new Object[]{String.format("%04X", hmsMessage.getHousecode()), hmsMessage.getSignalStrength()});
        try {
            Map<HmsProperty, DataPointRT> hmsDataPoints = getHmsPoints(hmsMessage);
            for (HmsProperty prop : hmsMessage.getDeviceType().getProperties()) {
                if (prop == HmsProperty.RAW_VALUE) {
                    //Just ignore
                    continue;
                }
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
        } catch (Throwable t) {
            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new LocalizableMessageImpl("event.exception2", vo.getName(), t.getMessage()));
            final LogRecord lr = new LogRecord(Level.SEVERE, "FHZ hms parsed: {0}");
            lr.setParameters(new Object[]{hmsMessage});
            lr.setThrown(t);
            LOG.log(lr);
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

    //TODO alphanumeric datatypes ???
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
        dp.setName(hmsLocator.defaultName());
        dp.setXid(String.format("%04x-%s-%s", hmsMessage.getHousecode(), hmsMessage.getDeviceType().getName(), prop.getName()));
        dp.setDataSourceId(vo.getId());
        dp.setEnabled(true);
        dp.setLoggingType(DataPointVO.LoggingTypes.ON_CHANGE);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());

        dp.setPointLocator(fhzLocator);
        if (dp.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", prop.getUnitOfMeasurement()));
            dp.setChartRenderer(new ImageChartRenderer(Common.TimePeriods.DAYS, 1));
        }

        DataPointRT dataPointRT = Common.ctx.getRuntimeManager().saveDataPoint(dp);
        Common.ctx.getRuntimeManager().addPointToHierarchy(dp, vo.getName(), hmsLocator.getHousecodeStr() + " " + hmsMessage.getDeviceType().getLabel());

        if (dataPointRT != null) {
            updateValue(dataPointRT, hmsMessage, prop);
        }
        LOG.log(Level.SEVERE, "HMS point added: {0}", dp.getXid());
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

        // this value was not found, so create one
        final Fhz4JPointLocatorVO<FhtTempPropery> fhzLocator = (Fhz4JPointLocatorVO<FhtTempPropery>) vo.createPontLocator(FhzProtocol.FHT_TEMP);
        final FhtMeasuredTempPointLocator fhtLocator = (FhtMeasuredTempPointLocator) fhzLocator.getProtocolLocator();
        fhtLocator.setHousecode(fhtTempMessage.getHousecode());
        fhtLocator.setFhtDeviceType(fhtTempMessage.getCommand().getSupportedBy()[0]);
        fhtLocator.setProperty(fhtTempMessage.getProperty());
        fhtLocator.setSettable(false);

        DataPointVO dp = new DataPointVO();
        dp.setName(fhtLocator.defaultName());
        dp.setEnabled(true);
        dp.setXid(String.format("%04x-combinedTemp", fhtTempMessage.getHousecode()));
        dp.setDataSourceId(vo.getId());
        dp.setLoggingType(DataPointVO.LoggingTypes.ALL);
        dp.setEventDetectors(new ArrayList<PointEventDetectorVO>());

        dp.setPointLocator(fhzLocator);
        if (dp.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            dp.setTextRenderer(new AnalogRenderer("#,##0.0", fhtTempMessage.getProperty().getUnitOfMeasurement()));
            dp.setChartRenderer(new ImageChartRenderer(Common.TimePeriods.DAYS, 1));
        }

        DataPointRT dataPointRT = Common.ctx.getRuntimeManager().saveDataPoint(dp);
        Common.ctx.getRuntimeManager().addPointToHierarchy(dp, vo.getName(), Fhz1000.houseCodeToString(fhtTempMessage.getHousecode()) + " FHT");

        if (dataPointRT != null) {
            updateValue(dataPointRT, fhtTempMessage, fhtTempMessage.getProperty());
        }
        LOG.log(Level.INFO, "FHT point added: {0}", dp.getName());
    }
}
