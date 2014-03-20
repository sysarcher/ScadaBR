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

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import net.sf.fhz4j.fht.FhtMeasuredTempMessage;
import net.sf.fhz4j.fht.FhtMessage;

import net.sf.fhz4j.hms.HmsMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataSource.EventDataSource;
import java.util.HashSet;
import java.util.Set;
import net.sf.fhz4j.FhzDataListener;
import net.sf.fhz4j.FhzParser;
import net.sf.fhz4j.FhzWriter;
import com.serotonin.mango.vo.dataSource.fhz4j.Fhz4JDataSourceVO;

/**
 *
 * TODO datatype NUMERIC_INT is missing TODO Starttime for timpepoints ???
 *
 */
public class Fhz4JDataSourceRT extends EventDataSource implements FhzDataListener {

    private final static Log LOG = LogFactory.getLog(Fhz4JDataSourceRT.class);
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int POINT_READ_EXCEPTION_EVENT = 2;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 3;

    // private final long nextRescan = 0;
    private SerialPort sPort;
    private FhzParser parser;
    private FhzWriter writer;
    private Fhz4JDataSourceVO vo;

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
            LOG.error(ex);
        }
        super.terminate();
    }

    @Override
    public synchronized void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
    }

    @Override
    public void fhtDataParsed(FhtMessage fhtMessage) {
        final long time = System.currentTimeMillis();
        for (DataPointRT point : dataPoints) {
            final Fhz4JPointLocatorRT locator = point.getPointLocator();
            if (locator.isMyFhtMessage(fhtMessage)) {
                switch (fhtMessage.getCommand()) {
                    case VALVE:
                    case VALVE_1:
                    case VALVE_2:
                    case VALVE_3:
                    case VALVE_4:
                    case VALVE_5:
                    case VALVE_6:
                    case VALVE_7:
                    case VALVE_8:
                        point.updatePointValue(new PointValueTime(fhtMessage.getActuatorValue(), time));
                        break;
                    case DESIRED_TEMP:
                        point.updatePointValue(new PointValueTime(fhtMessage.getDesiredTempValue(), time));
                        break;
                    case MEASURED_LOW:
                        point.updatePointValue(new PointValueTime(fhtMessage.getLowTempValue(), time));
                    case MEASURED_HIGH:
                        point.updatePointValue(new PointValueTime(fhtMessage.getHighTempValue(), time));
                        break;
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
//                point.updatePointValue(new PointValueTime(fhtMessage.getActuatorValue() , time));
                        break;
                    default:
                        point.updatePointValue(new PointValueTime(fhtMessage.getRawValue(), time));
                }
            }
        }
    }

    private short[] getFhtDeviceHousecodes() {
        Set<Short> housecodes = new HashSet<Short>();
        for (DataPointRT dp : dataPoints) {
            final Fhz4JPointLocatorRT loc = dp.getPointLocator();
            housecodes.add(loc.getHousecode());
        }
        short[] result = new short[housecodes.size()];
        int i = 0;
        for (short housecode : housecodes) {
            result[i++] = housecode;
        }
        return result;
    }

    @Override
    public void fhtMeasuredTempData(FhtMeasuredTempMessage temp) {
//TODO        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void hmsDataParsed(HmsMessage hmsMsg) {
//TODO        throw new UnsupportedOperationException("Not supported yet.");
    }

}
