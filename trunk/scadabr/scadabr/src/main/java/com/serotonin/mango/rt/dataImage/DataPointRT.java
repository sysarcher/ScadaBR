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
package com.serotonin.mango.rt.dataImage;

import br.org.scadabr.DataType;
import br.org.scadabr.utils.ImplementMeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.PointValueDao;
import br.org.scadabr.dao.SystemSettingsDao;
import br.org.scadabr.rt.SchedulerPool;
import br.org.scadabr.rt.event.schedule.ScheduledEventManager;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.event.detectors.PointEventDetectorRT;
import com.serotonin.mango.util.timeout.RunClient;
import com.serotonin.mango.view.stats.AnalogStatistics;
import com.serotonin.mango.view.stats.IValueTime;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import br.org.scadabr.timer.cron.DataSourceCronTask;
import br.org.scadabr.timer.cron.EventRunnable;
import br.org.scadabr.util.ILifecycle;
import br.org.scadabr.vo.IntervalLoggingTypes;
import br.org.scadabr.vo.LoggingTypes;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.dataImage.types.DoubleValue;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

//TODO split tist to datatypes Double ....

@Configurable
public class DataPointRT implements IDataPoint, ILifecycle, RunClient {

    private static final Log LOG = LogFactory.getLog(DataPointRT.class);
    private static final PvtTimeComparator pvtTimeComparator = new PvtTimeComparator();

    // Configuration data.
    private final DataPointVO vo;
    private final PointLocatorRT pointLocator;

    // Runtime data.
    private volatile PointValueTime<DoubleValue> pointValue;
    @Autowired
    private RuntimeManager runtimeManager;
    @Autowired
    private ScheduledEventManager scheduledEventManager;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private PointValueDao pointValueDao;
    @Autowired
    private SystemSettingsDao systemSettingsDao;
    @Autowired
    private SchedulerPool schedulerPool;
    private List<PointEventDetectorRT> detectors;
    private final Map<String, Object> attributes = new HashMap<>();

    // Interval logging data.
    private PointValueTime<DoubleValue> intervalValue;
    private long intervalStartTime = -1;
    private List<IValueTime> averagingValues;
    private final Object intervalLoggingLock = new Object();
    private DataSourceCronTask intervalLoggingTask;

    /**
     * This is the value around which tolerance decisions will be made when
     * determining whether to log numeric values.
     */
    private double toleranceOrigin;

    public DataPointRT(DataPointVO vo, PointLocatorRT pointLocator) {
        this.vo = vo;
        this.pointLocator = pointLocator;
    }

    /**
     * This method should only be called by the data source. Other types of
     * point setting should include a set point source object so that the
     * annotation can be logged.
     *
     * @param newValue
     */
    @Override
    public void updatePointValue(PointValueTime newValue) {
        savePointValue(newValue, null, true);
    }

    @Override
    public void updatePointValue(PointValueTime newValue, boolean async) {
        savePointValue(newValue, null, async);
    }

    /**
     * Use this method to update a data point for reasons other than just data
     * source update.
     *
     * @param newValue the value to set
     * @param source the source of the set. This can be a user object if the
     * point was set from the UI, or could be a program run by schedule or on
     * event.
     */
    @Override
    public void setPointValue(PointValueTime newValue, SetPointSource source) {
        if (source == null) {
            savePointValue(newValue, source, true);
        } else {
            savePointValue(newValue, source, false);
        }
    }

    private void savePointValue(PointValueTime<DoubleValue> newValue, SetPointSource source, boolean async) {
        // Null values are not very nice, and since they don't have a specific meaning they are hereby ignored.
        if (newValue == null) {
            return;
        }

        // Check the data type of the value against that of the locator, just for fun.
        DataType valueDataType = newValue.getDataType();
        if (valueDataType != DataType.UNKNOWN && valueDataType != vo.getDataType()) // This should never happen, but if it does it can have serious downstream consequences. Also, we need
        // to know how it happened, and the stack trace here provides the best information.
        {
            throw new ShouldNeverHappenException("Data type mismatch between new value and point locator: newValue="
                    + newValue.getDataType() + ", locator=" + vo.getDataType());
        }

        if (newValue.getTimestamp()> System.currentTimeMillis() + systemSettingsDao.getFutureDateLimit()) {
            // Too far future dated. Toss it. But log a message first.
            LOG.warn("Future dated value detected: pointId=" + vo.getId() + ", value=" + newValue.getValue()
                    + ", type=" + vo.getDataType() + ", ts=" + newValue.getTimestamp(), new Exception());
            return;
        }

        boolean backdated = pointValue != null && newValue.getTimestamp()< pointValue.getTimestamp();

        // Determine whether the new value qualifies for logging.
        boolean logValue;
        // ... or even saving in the cache.
        boolean saveValue = true;
        switch (vo.getLoggingType()) {
            case ON_CHANGE:
                if (pointValue == null) {
                    logValue = true;
                } else if (backdated) // Backdated. Ignore it
                {
                    logValue = false;
                } else {
                    if (newValue.getMangoValue() instanceof DoubleValue) {
                        // Get the new double
                        double newd = newValue.getMangoValue().getDoubleValue();

                        // See if the new value is outside of the tolerance.
                        double diff = toleranceOrigin - newd;
                        if (diff < 0) {
                            diff = -diff;
                        }

                        if (diff > vo.getTolerance()) {
                            toleranceOrigin = newd;
                            logValue = true;
                        } else {
                            logValue = false;
                        }
                    } else {
                        logValue = !Objects.equals(newValue.getValue(), pointValue.getValue());
                    }
                }

                saveValue = logValue;
                break;
            case ALL:
                logValue = true;
                break;
            case ON_TS_CHANGE:
                if (pointValue == null) {
                    logValue = true;
                } else if (backdated) // Backdated. Ignore it
                {
                    logValue = false;
                } else {
                    logValue = newValue.getTimestamp()!= pointValue.getTimestamp();
                }

                saveValue = logValue;
                break;
            case INTERVAL:
                if (!backdated) {
                    intervalSave(newValue);
                }
            default:
                logValue = false;
        }

        if (saveValue) {
            if (logValue) {
                if (async) {
                    pointValueDao.savePointValueAsync(newValue, source);
                } else {
                    pointValueDao.savePointValueSync(newValue, source);
                }
            }
        }

        // Ignore historical values.
        if (pointValue == null || newValue.getTimestamp()>= pointValue.getTimestamp()) {
            PointValueTime oldValue = pointValue;
            pointValue = newValue;
            fireEvents(oldValue, newValue, source != null, false);
        } else {
            fireEvents(null, newValue, false, true);
        }
    }

    //
    // / Interval logging
    //
    private void initializeIntervalLogging() {
        synchronized (intervalLoggingLock) {
            if (vo.getLoggingType() != LoggingTypes.INTERVAL) {
                return;
            }

            if (true) {
                throw new ImplementMeException(); //WAS: intervalLoggingTask = new TimeoutTask(this, vo.getIntervalLoggingPeriodType(), vo.getIntervalLoggingPeriod());
            }
            intervalValue = pointValue;
            if (vo.getIntervalLoggingType() == IntervalLoggingTypes.AVERAGE) {
                intervalStartTime = System.currentTimeMillis();
                averagingValues = new ArrayList<>();
            }
        }
    }

    private void terminateIntervalLogging() {
        synchronized (intervalLoggingLock) {
            if (vo.getLoggingType() != LoggingTypes.INTERVAL) {
                return;
            }

            intervalLoggingTask.cancel();
        }
    }

    private void intervalSave(PointValueTime<DoubleValue> pvt) {
        synchronized (intervalLoggingLock) {
            switch (vo.getIntervalLoggingType()) {
                case MAXIMUM:
                    if (intervalValue == null) {
                        intervalValue = pvt;
                    } else if (pvt != null) {
                        if (intervalValue.getMangoValue().getDoubleValue() < pvt.getMangoValue().getDoubleValue()) {
                            intervalValue = pvt;
                        }
                    }
                    break;
                case MINIMUM:
                    if (intervalValue == null) {
                        intervalValue = pvt;
                    } else if (pvt != null) {
                        if (intervalValue.getMangoValue().getDoubleValue() > pvt.getMangoValue().getDoubleValue()) {
                            intervalValue = pvt;
                        }
                    }
                    break;
                case AVERAGE:
                    averagingValues.add(pvt);
            }
        }
    }

    /**
     * Collect the data and store them
     *
     * @param fireTime
     */
    @Override
    public void run(long fireTime) {
        synchronized (intervalLoggingLock) {
            DoubleValue value;
            switch (vo.getIntervalLoggingType()) {
                case INSTANT:
                    value = PointValueTime.getValue(pointValue);
                    break;
                case MAXIMUM:
                case MINIMUM:
                    value = PointValueTime.getValue(intervalValue);
                    intervalValue = pointValue;
                    break;
                case AVERAGE:
                    AnalogStatistics stats = new AnalogStatistics(intervalValue, averagingValues, intervalStartTime,
                            fireTime);
                    value = new DoubleValue(stats.getAverage());

                    intervalValue = pointValue;
                    averagingValues.clear();
                    intervalStartTime = fireTime;
                    break;
                default:
                    throw new ShouldNeverHappenException("Unknown interval logging type: " + vo.getIntervalLoggingType());
            }

            if (value != null) {
                pointValueDao.savePointValueAsync(new PointValueTime(value, vo.getId(), fireTime), null);
            }
        }
    }

    //
    // /
    // / Properties
    // /
    //
    public int getId() {
        return vo.getId();
    }

    @Override
    public PointValueTime getPointValue() {
        return pointValue;
    }

    @SuppressWarnings("unchecked")
    public <T extends PointLocatorRT> T getPointLocator() {
        return (T) pointLocator;
    }

    public int getDataSourceId() {
        return vo.getDataSourceId();
    }

    public DataPointVO getVo() {
        return vo;
    }

    public String getVoName() {
        return vo.getName();
    }

    @Override
    public DataType getDataType() {
        return vo.getDataType();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + getId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataPointRT other = (DataPointRT) obj;
        return getId() == other.getId();
    }

    @Override
    public String toString() {
        return "DataPointRT(id=" + getId() + ", name=" + vo.getName() + ")";
    }

    //
    // /
    // / Listeners
    // /
    //
    private void fireEvents(PointValueTime oldValue, PointValueTime newValue, boolean set, boolean backdate) {
        DataPointListener l = runtimeManager.getDataPointListeners(vo.getId());
        if (l != null) {
            schedulerPool.execute(new EventNotifyWorkItem(l, oldValue, newValue, set, backdate));
        }
    }

    class EventNotifyWorkItem implements EventRunnable {

        private final DataPointListener listener;
        private final PointValueTime oldValue;
        private final PointValueTime newValue;
        private final boolean set;
        private final boolean backdate;

        EventNotifyWorkItem(DataPointListener listener, PointValueTime oldValue, PointValueTime newValue, boolean set,
                boolean backdate) {
            this.listener = listener;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.set = set;
            this.backdate = backdate;
        }

        @Override
        public void run() {
            if (backdate) {
                listener.pointBackdated(newValue);
            } else {
                // Always fire this.
                listener.pointUpdated(newValue);

                // Fire if the point has changed.
                if (!PointValueTime.equalValues(oldValue, newValue)) {
                    listener.pointChanged(oldValue, newValue);
                }

                // Fire if the point was set.
                if (set) {
                    listener.pointSet(oldValue, newValue);
                }
            }
        }
/*
        @Override
        public int getPriority() {
            return WorkItem.PRIORITY_MEDIUM;
        }
        */
    }

    //
    //
    // Lifecycle
    //
    @Override
    public void initialize() {
        // Get the latest value for the point from the database.
        pointValue = pointValueDao.getLatestPointValue(vo.getId());

        // Set the tolerance origin if this is a numeric
        if (pointValue != null && pointValue.getMangoValue() instanceof DoubleValue) {
            toleranceOrigin = pointValue.getMangoValue().getDoubleValue();
        }

        // Add point event listeners
        for (PointEventDetectorVO ped : vo.getEventDetectors()) {
            if (detectors == null) {
                detectors = new ArrayList<>();
            }

            PointEventDetectorRT pedRT = ped.createRuntime();
            detectors.add(pedRT);
            scheduledEventManager.addPointEventDetector(pedRT);
            runtimeManager.addDataPointListener(vo.getId(), pedRT);
        }

        initializeIntervalLogging();
    }

    @Override
    public void terminate() {
        terminateIntervalLogging();

        //TODO notify runtimeManger and lat them handle this???
        if (detectors != null) {
            for (PointEventDetectorRT pedRT : detectors) {
                runtimeManager.removeDataPointListener(vo.getId(), pedRT);
                scheduledEventManager.removePointEventDetector(pedRT.getEventDetectorKey());
            }
        }
        //TODO notify runtimeManger and lat them handle this???
        eventManager.cancelEventsForDataPoint(vo.getId());
    }

    @Override
    public void joinTermination() {
        // no op
    }

    public void initializeHistorical() {
        initializeIntervalLogging();
    }

    public void terminateHistorical() {
        terminateIntervalLogging();
    }
}
