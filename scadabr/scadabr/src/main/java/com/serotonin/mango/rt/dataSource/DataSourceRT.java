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
package com.serotonin.mango.rt.dataSource;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.rt.IDataPointLiveCycleListener;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.event.type.DataSourceEventType;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import br.org.scadabr.util.ILifecycle;
import br.org.scadabr.i18n.LocalizableException;
import br.org.scadabr.i18n.LocalizableMessage;
import br.org.scadabr.i18n.LocalizableMessageImpl;
import com.serotonin.mango.vo.DataPointVO;

/**
 * Data sources are things that produce data for consumption of this system.
 * Anything that houses, creates, manages, or otherwise can get data to Mango
 * can be considered a data source. As such, this interface can more precisely
 * be considered a proxy of the real thing.
 *
 * Mango contains multiple objects that carry the name data source. This
 * interface represents those types of objects that execute and perform the
 * actual task of getting information one way or another from the external data
 * source and into the system, and is known as the "run-time" (RT) data source.
 * (Another type is the data source VO, which represents the configuration of a
 * data source RT, a subtle but useful distinction. In particular, a VO is
 * serializable, while an RT is not.)
 *
 * @author Matthew Lohbihler
 */
abstract public class DataSourceRT<T extends DataSourceVO<T>> implements ILifecycle, IDataPointLiveCycleListener {

    public static final String ATTR_UNRELIABLE_KEY = "UNRELIABLE";
    protected final T vo;
    private final List<DataSourceEventType> eventTypes;
    /**
     * Under the expectation that most data sources will run in their own
     * threads, the addedPoints field is used as a cache for points that have
     * been added to the data source, so that at a convenient time for the data
     * source they can be included in the polling.
     *
     * Access should be synchronized with the monitor of addedChangedPoints
     *
     * Note that updated versions of data points that could already be running
     * may be added here, so implementations should always check for existing
     * instances.
     */
    protected final Map<Integer, DataPointRT> enabledDataPointsCache = new HashMap<>();
    protected final Map<Integer, DataPointRT> enabledDataPoints = new HashMap<>();

    /**
     * Under the expectation that most data sources will run in their own
     * threads, the removedPoints field is used as a cache for points that have
     * been removed from the data source, so that at a convenient time for the
     * data source they can be removed from the polling.
     *
     * Access should be synchronized with the monitor of removedPoints
     *
     */
    protected final List<DataPointVO> disabledDataPointsCache = new ArrayList<>();
    protected final Map<Integer, DataPointVO> disabledDataPoints = new HashMap<>();

    protected final List<DataPointVO> deletedDataPointsCache = new ArrayList<>();

    protected final Object dataPointsCacheLock = new Object();
    private final boolean caching;
    private boolean cacheChanged;
    protected boolean enabledDataPointsChanged;

    /**
     *
     * @param vo
     * @param doCache whether or not enabling/disabling of datapoints will be
     * cached
     */
    public DataSourceRT(T vo, boolean doCache) {
        this.vo = vo;
        caching = doCache;

        eventTypes = new ArrayList<>();
        for (EventTypeVO etvo : vo.getEventTypes()) {
            eventTypes.add((DataSourceEventType) etvo.createEventType());
        }
    }

    public int getId() {
        return vo.getId();
    }

    public String getName() {
        return vo.getName();
    }

    /**
     * This method is usable by subclasses to retrieve serializable data stored
     * using the setPersistentData method.
     */
    public Object getPersistentData() {
        return DataSourceDao.getInstance().getPersistentData(vo.getId());
    }

    /**
     * This method is usable by subclasses to store any type of serializable
     * data. This intention is to provide a mechanism for data source RTs to be
     * able to persist data between runs. Normally this method would at least be
     * called in the terminate method, but may also be called regularly for
     * failover purposes.
     */
    protected void setPersistentData(Object persistentData) {
        DataSourceDao.getInstance().savePersistentData(vo.getId(), persistentData);
    }

    /*
     * add activated DataPoints to this datasource
     */
    @Override
    public void dataPointEnabled(DataPointRT dataPoint) {
        synchronized (dataPointsCacheLock) {
            if (caching) {
                cacheChanged |= enabledDataPointsCache.put(dataPoint.getId(), dataPoint) == null;
                cacheChanged |= disabledDataPointsCache.remove(dataPoint.getVo());
                cacheChanged |= deletedDataPointsCache.remove(dataPoint.getVo());
            } else {
                enabledDataPoints.put(dataPoint.getId(), dataPoint);
                disabledDataPoints.remove(dataPoint.getId());
            }
        }
    }

    /*
     * remove disabled DataPoints from this datasource
     */
    @Override
    public void dataPointDisabled(DataPointVO dataPoint) {
        synchronized (dataPointsCacheLock) {
            if (caching) {
                cacheChanged |= enabledDataPointsCache.remove(dataPoint.getId()) != null;
                cacheChanged |= disabledDataPointsCache.add(dataPoint);
                cacheChanged |= deletedDataPointsCache.remove(dataPoint);
            } else {
                enabledDataPoints.remove(dataPoint.getId());
                disabledDataPoints.put(dataPoint.getId(), dataPoint);
            }
        }
    }

    /*
     * remove disabled DataPoints from this datasource
     */
    @Override
    public void dataPointDeleted(DataPointVO dataPoint) {
        synchronized (dataPointsCacheLock) {
            if (caching) {
                cacheChanged |= enabledDataPointsCache.remove(dataPoint.getId()) != null;
                cacheChanged |= disabledDataPointsCache.remove(dataPoint);
                cacheChanged |= deletedDataPointsCache.add(dataPoint);
            } else {
                enabledDataPoints.remove(dataPoint.getId());
                disabledDataPoints.remove(dataPoint.getId());
            }
        }
    }

    /**
     * No really need to synchronize with #cacheChanged
     */
    protected void updateChangedPoints() {
        if (!cacheChanged) {
            return;
        }
        cacheChanged = false;
        synchronized (dataPointsCacheLock) {
            enabledDataPoints.putAll(enabledDataPointsCache);
            enabledDataPointsCache.clear();
            enabledDataPointsChanged = true;
            for (DataPointVO dpVo : disabledDataPointsCache) {
                disabledDataPoints.remove(dpVo.getId());
            }
            disabledDataPointsCache.clear();
        }
    }

    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no Op
    }

    public void relinquish(DataPointRT dataPoint) {
        throw new ShouldNeverHappenException("not implemented in " + getClass());
    }

    public void forcePointRead(@SuppressWarnings("unused") DataPointRT dataPoint) {
        // No op by default. Override as required.
    }

    protected void raiseEvent(int eventId, long time, boolean rtn, LocalizableMessage message) {
        message = new LocalizableMessageImpl("event.ds", vo.getName(), message);
        DataSourceEventType type = getEventType(eventId);

        Map<String, Object> context = new HashMap<>();
        context.put("dataSource", vo);

        Common.ctx.getEventManager().raiseEvent(type, time, rtn, type.getAlarmLevel(), message, context);
    }

    protected void returnToNormal(int eventId, long time) {
        DataSourceEventType type = getEventType(eventId);
        Common.ctx.getEventManager().returnToNormal(type, time);
    }

    private DataSourceEventType getEventType(int eventId) {
        for (DataSourceEventType et : eventTypes) {
            if (et.getDataSourceEventTypeId() == eventId) {
                return et;
            }
        }
        return null;
    }

    public static LocalizableException wrapSerialException(Exception e, String portId) {
        if (e instanceof NoSuchPortException) {
            return new LocalizableException("event.serial.portOpenError", portId);
        }
        if (e instanceof PortInUseException) {
            return new LocalizableException("event.serial.portInUse", portId);
        }
        return wrapException(e);
    }

    public static LocalizableException wrapException(Exception e) {
        return new LocalizableException("event.exception2", e.getClass().getName(), e.getMessage());
    }

    //
    // /
    // / Lifecycle
    // /
    //
    @Override
    public void initialize() {
        // no op
    }

    @Override
    public void terminate() {
        // Remove any outstanding events.
        Common.ctx.getEventManager().cancelEventsForDataSource(vo.getId());
    }

    @Override
    public void joinTermination() {
        // no op
    }

    //
    // Additional lifecycle.
    public void beginPolling() {
        // no op
    }

}
