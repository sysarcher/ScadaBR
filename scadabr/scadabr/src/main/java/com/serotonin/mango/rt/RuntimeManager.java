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
package com.serotonin.mango.rt;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.MaintenanceEventDao;
import com.serotonin.mango.db.dao.PointLinkDao;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.db.dao.ScheduledEventDao;
import com.serotonin.mango.rt.dataImage.DataPointEventMulticaster;
import com.serotonin.mango.rt.dataImage.DataPointListener;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.meta.MetaDataSourceRT;
import com.serotonin.mango.rt.event.SimpleEventDetector;
import com.serotonin.mango.rt.event.compound.CompoundEventDetectorRT;
import com.serotonin.mango.rt.event.detectors.PointEventDetectorRT;
import com.serotonin.mango.rt.event.maintenance.MaintenanceEventRT;
import com.serotonin.mango.rt.event.schedule.ScheduledEventRT;
import com.serotonin.mango.rt.link.PointLinkRT;
import com.serotonin.mango.rt.publish.PublisherRT;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.util.DateUtils;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.CompoundEventDetectorVO;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.mango.vo.event.ScheduledEventVO;
import com.serotonin.mango.vo.link.PointLinkVO;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.util.LifecycleException;
import com.serotonin.web.i18n.LocalizableException;
import com.serotonin.web.i18n.LocalizableMessage;

@Service
public class RuntimeManager {

    private final static Logger LOG = LoggerFactory.getLogger(RuntimeManager.class);
    //TODO use Set??
    private final List<DataSourceRT> runningDataSources = new CopyOnWriteArrayList();
    @Autowired
    private PointValueDao pointValueDao;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private DataSourceDao dataSourceDao;
    @Autowired
    private PointLinkDao pointLinkDao;
    @Autowired
    private ScheduledEventDao scheduledEventDao;
    @Autowired
    private CompoundEventDetectorDao compoundEventDetectorDao;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    private MaintenanceEventDao maintenanceEventDao;
    @Autowired
    private DataPointDao dataPointDao;
    

    /**
     * Provides a quick lookup map of the running data points.
     */
    private final Map<Integer, DataPointRT> dataPoints = new ConcurrentHashMap();
    /**
     * The list of point listeners, kept here such that listeners can be
     * notified of point initializations (i.e. a listener can register itself
     * before the point is enabled).
     */
    //TODO DataPointRT instead of Integer???
    private final Map<Integer, DataPointListener> dataPointListeners = new ConcurrentHashMap();
    /**
     * Store of enabled event detectors.
     */
    private final Map<String, SimpleEventDetector> simpleEventDetectors = new ConcurrentHashMap();
    /**
     * Store of enabled compound event detectors.
     */
    private final Map<Integer, CompoundEventDetectorRT> compoundEventDetectors = new ConcurrentHashMap();
    /**
     * Store of enabled publishers
     */
    private final List<PublisherRT<?>> runningPublishers = new CopyOnWriteArrayList();
    /**
     * Store of enabled point links
     */
    private final List<PointLinkRT> pointLinks = new CopyOnWriteArrayList();
    /**
     * Store of maintenance events
     */
    private final List<MaintenanceEventRT> maintenanceEvents = new CopyOnWriteArrayList();
    private boolean started = false;

    //
    // Lifecycle
    @PostConstruct
    synchronized public void initialize() {
        if (started) {
            throw new ShouldNeverHappenException(
                    "RuntimeManager already started");
        }
        // Check for safe mode.
        File safeFile = new File("SAFE");
        boolean safe = false;
        if (safeFile.exists() && safeFile.isFile()) {
            // Indicate that we're in safe mode.
            StringBuilder sb = new StringBuilder();
            sb.append("\r\n");
            sb.append("*********************************************************\r\n");
            sb.append("*                    NOTE                               *\r\n");
            sb.append("*********************************************************\r\n");
            sb.append("* Mango M2M is starting in safe mode. All data sources, *\r\n");
            sb.append("* point links, scheduled events, compound events, and   *\r\n");
            sb.append("* publishers will be disabled. To disable safe mode,    *\r\n");
            sb.append("* remove the SAFE file from the Mango M2M application   *\r\n");
            sb.append("* directory.                                            *\r\n");
            sb.append("*                                                       *\r\n");
            sb.append("* To find all objects that were automatically disabled, *\r\n");
            sb.append("* search for Audit Events on the alarms page.           *\r\n");
            sb.append("*********************************************************");
            LOG.warn(sb.toString());
            safe = true;
        }

        try {
            if (safe) {
                BackgroundContext.set("common.safeMode");
            }
        } catch (Exception e) {
            LOG.error("RuntimeManager initialization failure", e);
        } finally {
            if (safe) {
                BackgroundContext.remove();
            }
        }

        // Set the started indicator to true.
        started = true;

        // Initialize data sources that are enabled.
        List<DataSourceVO<?>> configs = dataSourceDao.getDataSources();
        List<DataSourceVO<?>> pollingRound = new ArrayList();
        for (DataSourceVO<?> config : configs) {
            if (config.isEnabled()) {
                if (safe) {
                    config.setEnabled(false);
                    dataSourceDao.saveDataSource(config);
                } else if (initializeDataSource(config)) {
                    pollingRound.add(config);
                }
            }
        }

        // Set up point links.
        for (PointLinkVO vo : pointLinkDao.getPointLinks()) {
            if (!vo.isDisabled()) {
                if (safe) {
                    vo.setDisabled(true);
                    pointLinkDao.savePointLink(vo);
                } else {
                    startPointLink(vo);
                }
            }
        }

        // Tell the data sources to start polling. Delaying the polling start
        // gives the data points a chance to
        // initialize such that point listeners in meta points and set point
        // handlers can run properly.
        for (DataSourceVO<?> config : pollingRound) {
            startDataSourcePolling(config);
        }

        // Initialize the scheduled events.
        List<ScheduledEventVO> scheduledEvents = scheduledEventDao.getScheduledEvents();
        for (ScheduledEventVO se : scheduledEvents) {
            if (!se.isDisabled()) {
                if (safe) {
                    se.setDisabled(true);
                    scheduledEventDao.saveScheduledEvent(se);
                } else {
                    startScheduledEvent(se);
                }
            }
        }

        // Initialize the compound events.
        List<CompoundEventDetectorVO> compoundDetectors = compoundEventDetectorDao.getCompoundEventDetectors();
        for (CompoundEventDetectorVO ced : compoundDetectors) {
            if (!ced.isDisabled()) {
                if (safe) {
                    ced.setDisabled(true);
                    compoundEventDetectorDao.saveCompoundEventDetector(ced);
                } else {
                    startCompoundEventDetector(ced);
                }
            }
        }

        // Start the publishers that are enabled
        List<PublisherVO<? extends PublishedPointVO>> publishers = publisherDao.getPublishers();
        for (PublisherVO<? extends PublishedPointVO> vo : publishers) {
            if (vo.isEnabled()) {
                if (safe) {
                    vo.setEnabled(false);
                    publisherDao.savePublisher(vo);
                } else {
                    startPublisher(vo);
                }
            }
        }

        // Start the maintenance events that are enabled
        for (MaintenanceEventVO vo : maintenanceEventDao.getMaintenanceEvents()) {
            if (!vo.isDisabled()) {
                if (safe) {
                    vo.setDisabled(true);
                    maintenanceEventDao.saveMaintenanceEvent(vo);
                } else {
                    startMaintenanceEvent(vo);
                }
            }
        }
    }

    synchronized public void terminate() {
        if (!started) {
            throw new ShouldNeverHappenException(
                    "RuntimeManager not yet started");
        }

        started = false;
        for (MaintenanceEventRT me : maintenanceEvents) {
            stopMaintenanceEvent(me.getVo().getId());
        }

        for (PublisherRT<? extends PublishedPointVO> publisher : runningPublishers) {
            stopPublisher(publisher.getId());
        }

        for (Integer id : compoundEventDetectors.keySet()) {
            stopCompoundEventDetector(id);
        }

        for (PointLinkRT pointLink : pointLinks) {
            stopPointLink(pointLink.getId());
        }

        // First stop meta data sources.
        for (DataSourceRT dataSource : runningDataSources) {
            if (dataSource instanceof MetaDataSourceRT) {
                stopDataSource(dataSource);
            }
        }
        // Then stop everything else.
        for (DataSourceRT dataSource : runningDataSources) {
            stopDataSource(dataSource);
        }

        for (String key : simpleEventDetectors.keySet()) {
            stopSimpleEventDetector(key);
        }
    }

    public void joinTermination() {
        for (DataSourceRT dataSource : runningDataSources) {
            try {
                dataSource.joinTermination();
            } catch (ShouldNeverHappenException e) {
                LOG.error("Error stopping data source " + dataSource.getName(), e);
            }
        }
    }

    //
    //
    // Data sources
    //
    public DataSourceRT getRunningDataSource(DataSourceVO<?> dataSourceVo) {
        for (DataSourceRT dataSourceRt : runningDataSources) {
            if (dataSourceRt.getVo().equals(dataSourceVo)) {
                return dataSourceRt;
            }
        }
        return null;
    }

    private DataSourceRT getRunningDataSource(DataPointVO point) {
        for (DataSourceRT dataSourceRt : runningDataSources) {
            if (dataSourceRt.getId() == point.getDataSourceId()) {
                return dataSourceRt;
            }
        }
        return null;
    }

    private DataSourceRT getRunningDataSource(DataPointRT point) {
        for (DataSourceRT dataSourceRt : runningDataSources) {
            if (dataSourceRt.getId() == point.getDataSourceId()) {
                return dataSourceRt;
            }
        }
        return null;
    }

    public boolean isDataSourceRunning(DataSourceVO<?> dataSourceVo) {
        return getRunningDataSource(dataSourceVo) != null;
    }

    public List<DataSourceVO<?>> getDataSources() {
        return new DataSourceDao().getDataSources();
    }

    public DataSourceVO<?> getDataSource(int dataSourceId) {
        return new DataSourceDao().getDataSource(dataSourceId);
    }

    public void deleteDataSource(DataSourceVO<?> dataSource) {
        stopDataSource(dataSource);
        new DataSourceDao().deleteDataSource(dataSource);
        eventManager.cancelEventsForDataSource(dataSource);
    }

    public void saveDataSource(DataSourceVO<?> vo) {
        // If the data source is running, stop it.
        stopDataSource(vo);

        // In case this is a new data source, we need to save to the database
        // first so that it has a proper id.
        new DataSourceDao().saveDataSource(vo);

        // If the data source is enabled, start it.
        if (vo.isEnabled()) {
            if (initializeDataSource(vo)) {
                startDataSourcePolling(vo);
            }
        }
    }

    private boolean initializeDataSource(DataSourceVO<?> vo) {
        synchronized (runningDataSources) {
            // If the data source is already running, just quit.
            if (isDataSourceRunning(vo)) {
                return false;
            }

            // Ensure that the data source is enabled.
            Assert.isTrue(vo.isEnabled());

            // Create and initialize the runtime version of the data source.
            DataSourceRT dataSource = vo.createDataSourceRT();
            dataSource.initialize();

            // Add it to the list of running data sources.
            runningDataSources.add(dataSource);

            // Add the enabled points to the data source.
            List<DataPointVO> dataSourcePoints = dataPointDao.getDataPoints(vo, null);
            for (DataPointVO dataPoint : dataSourcePoints) {
                if (dataPoint.isEnabled()) {
                    startDataPoint(dataPoint);
                } else {
                    addDisabledDataPointToRT(dataPoint);
                }
            }
            LOG.info("Data source '" + vo.getName() + "' initialized");

            return true;
        }
    }

    private void startDataSourcePolling(DataSourceVO<?> vo) {
        DataSourceRT dataSource = getRunningDataSource(vo);
        if (dataSource != null) {
            dataSource.beginPolling();
        }
    }

    public void stopDataSource(DataSourceVO<?> dataSourceVo) {
        synchronized (runningDataSources) {
            DataSourceRT dataSourceRt = getRunningDataSource(dataSourceVo);
            if (dataSourceRt == null) {
                return;
            }

            // Stop the data points.
            for (DataPointRT p : dataPoints.values()) {
                if (p.getDataSourceId() == dataSourceVo.getId()) {
                    stopDataPoint(p);
                }
            }

            runningDataSources.remove(dataSourceRt);
            dataSourceRt.terminate();

            dataSourceRt.joinTermination();
            LOG.info("Data source '" + dataSourceRt.getName() + "' stopped");
        }
    }

    public void stopDataSource(DataSourceRT dataSourceRt) {
        synchronized (runningDataSources) {

            // Stop the data points.
            for (DataPointRT p : dataPoints.values()) {
                if (p.getDataSourceId() == dataSourceRt.getId()) {
                    stopDataPoint(p);
                }
            }

            runningDataSources.remove(dataSourceRt);
            dataSourceRt.terminate();

            dataSourceRt.joinTermination();
            LOG.info("Data source '" + dataSourceRt.getName() + "' stopped");
        }
    }

    public void addPointToHierarchy(DataPointVO dp, String... pathToPoint) {
        new DataPointDao().addPointToHierarchy(dp, pathToPoint);
    }

    /**
     * returns the started DataPoiuntRT if ists enabled...
     *
     */
    public DataPointRT saveDataPoint(DataPointVO point) {
        stopDataPoint(point);

        // Since the point's data type may have changed, we must ensure that the
        // other attrtibutes are still ok with
        // it.
        final MangoDataType dataType = point.getPointLocator().getMangoDataType();

        // Chart renderer
        if (point.getChartRenderer() != null
                && !point.getChartRenderer().getDef().supports(dataType)) // Return to a default renderer
        {
            point.setChartRenderer(null);
        }

        // Text renderer
        if (point.getTextRenderer() != null
                && !point.getTextRenderer().getDef().supports(dataType)) // Return to a default renderer
        {
            point.defaultTextRenderer();
        }

        // Event detectors
        Iterator<PointEventDetectorVO> peds = point.getEventDetectors().iterator();
        while (peds.hasNext()) {
            PointEventDetectorVO ped = peds.next();
            if (!ped.getDef().supports(dataType)) // Remove the detector.
            {
                peds.remove();
            }
        }

        new DataPointDao().saveDataPoint(point);

        if (point.isEnabled()) {
            return startDataPoint(point);
        } else {
            addDisabledDataPointToRT(point);
            return null;
        }
    }

    public void deleteDataPoint(DataPointVO point) {
        if (point.isEnabled()) {
            stopDataPoint(point);
        }
        DataSourceRT ds = getRunningDataSource(point);
        if (ds != null) {
            ds.dataPointDeleted(point);
        }
        new DataPointDao().deleteDataPoint(point);
        eventManager.cancelEventsForDataPoint(point);
    }

    private DataPointRT startDataPoint(DataPointVO vo) {
        synchronized (dataPoints) {
            Assert.isTrue(vo.isEnabled());

            // Only add the data point if its data source is enabled.
            DataSourceRT ds = getRunningDataSource(vo);
            if (ds != null) {
                // Change the VO into a data point implementation.
                DataPointRT dataPoint = new DataPointRT(vo, vo.getPointLocator().createRuntime());

                // Add/update it in the data image.
                dataPoints.put(dataPoint.getId(), dataPoint);

                // Initialize it.
                dataPoint.initialize();
                DataPointListener l = getDataPointListeners(vo);
                if (l != null) {
                    l.pointInitialized();
                }

                // Add/update it in the data source.
                ds.dataPointEnabled(dataPoint);
                return dataPoint;
            }
        }
        return null;
    }

    /**
     * add a disabled datapoint to a running datasource
     * @param vo
     * @return
     */
    private void addDisabledDataPointToRT(DataPointVO vo) {
        synchronized (dataPoints) {
            Assert.isTrue(!vo.isEnabled());

            // Only add the data point if its data source is enabled.
            DataSourceRT ds = getRunningDataSource(vo);
            if (ds != null) {
                ds.addDisabledDataPoint(vo);
            }
        }
        return;
    }
    
    private void stopDataPoint(DataPointVO dataPointVo) {
        synchronized (dataPoints) {
            stopDataPoint(dataPoints.get(dataPointVo.getId()));
        }
    }

    private void stopDataPoint(DataPointRT dataPointRt) {
        synchronized (dataPoints) {
            // Remove this point from the data image if it is there. If not,
            // just quit.
            DataPointRT p = dataPoints.remove(dataPointRt.getId());


            // Remove it from the data source, and terminate it.
            if (p != null) {
                DataSourceRT dsRT = getRunningDataSource(p);
                dsRT.dataPointDisabled(p);
                DataPointListener l = getDataPointListeners(dataPointRt);
                if (l != null) {
                    l.pointTerminated();
                }
                p.terminate();
            }
        }
    }

    public boolean isDataPointRunning(int dataPointId) {
        return dataPoints.get(dataPointId) != null;
    }

    public DataPointRT getDataPoint(int dataPointId) {
        return dataPoints.get(dataPointId);
    }

    public void addDataPointListener(int dataPointId, DataPointListener l) {
        DataPointListener listeners = dataPointListeners.get(dataPointId);
        dataPointListeners.put(dataPointId,
                DataPointEventMulticaster.add(listeners, l));
    }

    @Deprecated //TODO use not id but DataPointVO|RT
    public void removeDataPointListener(int dataPointId, DataPointListener l) {
        DataPointListener listeners = DataPointEventMulticaster.remove(
                dataPointListeners.get(dataPointId), l);
        if (listeners == null) {
            dataPointListeners.remove(dataPointId);
        } else {
            dataPointListeners.put(dataPointId, listeners);
        }
    }

    public DataPointListener getDataPointListeners(DataPointRT dataPoint) {
        return dataPointListeners.get(dataPoint.getId());
    }

    public DataPointListener getDataPointListeners(DataPointVO dataPoint) {
        return dataPointListeners.get(dataPoint.getId());
    }

    //
    // Point values
    public void setDataPointValue(int dataPointId, MangoValue value,
            SetPointSource source) {
        setDataPointValue(dataPointId,
                new PointValueTime(value, System.currentTimeMillis()), source);
    }

    public void setDataPointValue(int dataPointId, PointValueTime valueTime,
            SetPointSource source) {
        DataPointRT dataPoint = dataPoints.get(dataPointId);
        if (dataPoint == null) {
            throw new RTException("Point is not enabled");
        }

        if (!dataPoint.getPointLocator().isSettable()) {
            throw new RTException("Point is not settable");
        }

        // Tell the data source to set the value of the point.
        DataSourceRT ds = getRunningDataSource(dataPoint);
        // The data source may have been disabled. Just make sure.
        if (ds != null) {
            ds.setPointValue(dataPoint, valueTime, source);
        }
    }

    public void relinquish(int dataPointId) {
        DataPointRT dataPoint = dataPoints.get(dataPointId);
        if (dataPoint == null) {
            throw new RTException("Point is not enabled");
        }

        if (!dataPoint.getPointLocator().isSettable()) {
            throw new RTException("Point is not settable");
        }
        if (!dataPoint.getPointLocator().isRelinquishable()) {
            throw new RTException("Point is not relinquishable");
        }

        // Tell the data source to relinquish value of the point.
        DataSourceRT ds = getRunningDataSource(dataPoint);
        // The data source may have been disabled. Just make sure.
        if (ds != null) {
            ds.relinquish(dataPoint);
        }
    }

    public void forcePointRead(int dataPointId) {
        DataPointRT dataPoint = dataPoints.get(dataPointId);
        if (dataPoint == null) {
            throw new RTException("Point is not enabled");
        }

        // Tell the data source to read the point value;
        DataSourceRT ds = getRunningDataSource(dataPoint);
        if (ds != null) // The data source may have been disabled. Just make sure.
        {
            ds.forcePointRead(dataPoint);
        }
    }

    public long purgeDataPointValues() {
        long count = pointValueDao.deleteAllPointData();
        for (Integer id : dataPoints.keySet()) {
            updateDataPointValuesRT(id);
        }
        return count;
    }

    public long purgeDataPointValues(int dataPointId, int periodType,
            int periodCount) {
        long before = DateUtils.minus(System.currentTimeMillis(), periodType,
                periodCount);
        return purgeDataPointValues(dataPointId, before);
    }

    public long purgeDataPointValues(int dataPointId) {
        long count = new PointValueDao().deletePointValues(dataPointId);
        updateDataPointValuesRT(dataPointId);
        return count;
    }

    public long purgeDataPointValues(int dataPointId, long before) {
        long count = new PointValueDao().deletePointValuesBefore(dataPointId,
                before);
        if (count > 0) {
            updateDataPointValuesRT(dataPointId);
        }
        return count;
    }

    private void updateDataPointValuesRT(int dataPointId) {
        DataPointRT dataPoint = dataPoints.get(dataPointId);
        if (dataPoint != null) // Enabled. Reset the point's cache.
        {
            dataPoint.resetValues();
        }
    }

    //
    //
    // Scheduled events
    //
    public void saveScheduledEvent(ScheduledEventVO vo) {
        // If the scheduled event is running, stop it.
        stopSimpleEventDetector(vo.getEventDetectorKey());

        new ScheduledEventDao().saveScheduledEvent(vo);

        // If the scheduled event is enabled, start it.
        if (!vo.isDisabled()) {
            startScheduledEvent(vo);
        }
    }

    private void startScheduledEvent(ScheduledEventVO vo) {
        synchronized (simpleEventDetectors) {
            stopSimpleEventDetector(vo.getEventDetectorKey());
            ScheduledEventRT rt = vo.createRuntime();
            simpleEventDetectors.put(vo.getEventDetectorKey(), rt);
            rt.initialize();
        }
    }

    public void stopSimpleEventDetector(String key) {
        synchronized (simpleEventDetectors) {
            SimpleEventDetector rt = simpleEventDetectors.remove(key);
            if (rt != null) {
                rt.terminate();
            }
        }
    }

    //
    //
    // Point event detectors
    //
    public void addPointEventDetector(PointEventDetectorRT ped) {
        synchronized (simpleEventDetectors) {
            ped.initialize();
            simpleEventDetectors.put(ped.getEventDetectorKey(), ped);
        }
    }

    public void removePointEventDetector(String pointEventDetectorKey) {
        synchronized (simpleEventDetectors) {
            SimpleEventDetector sed = simpleEventDetectors.remove(pointEventDetectorKey);
            if (sed != null) {
                sed.terminate();
            }
        }
    }

    public SimpleEventDetector getSimpleEventDetector(String key) {
        return simpleEventDetectors.get(key);
    }

    //
    //
    // Compound event detectors
    //
    public boolean saveCompoundEventDetector(CompoundEventDetectorVO vo) {
        // If the CED is running, stop it.
        stopCompoundEventDetector(vo.getId());

        new CompoundEventDetectorDao().saveCompoundEventDetector(vo);

        // If the scheduled event is enabled, start it.
        if (!vo.isDisabled()) {
            return startCompoundEventDetector(vo);
        }

        return true;
    }

    public boolean startCompoundEventDetector(CompoundEventDetectorVO ced) {
        stopCompoundEventDetector(ced.getId());
        CompoundEventDetectorRT rt = ced.createRuntime();
        try {
            rt.initialize();
            compoundEventDetectors.put(ced.getId(), rt);
            return true;
        } catch (LifecycleException e) {
            rt.raiseFailureEvent(new LocalizableMessage(
                    "event.compound.exceptionFailure", ced.getName(),
                    ((LocalizableException) e.getCause()).getLocalizableMessage()));
        } catch (Exception e) {
            rt.raiseFailureEvent(new LocalizableMessage(
                    "event.compound.exceptionFailure", ced.getName(), e.getMessage()));
        }
        return false;
    }

    public void stopCompoundEventDetector(int compoundEventDetectorId) {
        CompoundEventDetectorRT rt = compoundEventDetectors.remove(compoundEventDetectorId);
        if (rt != null) {
            rt.terminate();
        }
    }

    //
    //
    // Publishers
    //
    public PublisherRT<?> getRunningPublisher(int publisherId) {
        for (PublisherRT<?> publisher : runningPublishers) {
            if (publisher.getId() == publisherId) {
                return publisher;
            }
        }
        return null;
    }

    public boolean isPublisherRunning(int publisherId) {
        return getRunningPublisher(publisherId) != null;
    }

    public PublisherVO<? extends PublishedPointVO> getPublisher(int publisherId) {
        return new PublisherDao().getPublisher(publisherId);
    }

    public void deletePublisher(int publisherId) {
        stopPublisher(publisherId);
        new PublisherDao().deletePublisher(publisherId);
        eventManager.cancelEventsForPublisher(publisherId);
    }

    public void savePublisher(PublisherVO<? extends PublishedPointVO> vo) {
        // If the data source is running, stop it.
        stopPublisher(vo.getId());

        // In case this is a new publisher, we need to save to the database
        // first so that it has a proper id.
        new PublisherDao().savePublisher(vo);

        // If the publisher is enabled, start it.
        if (vo.isEnabled()) {
            startPublisher(vo);
        }
    }

    private void startPublisher(PublisherVO<? extends PublishedPointVO> vo) {
        synchronized (runningPublishers) {
            // If the publisher is already running, just quit.
            if (isPublisherRunning(vo.getId())) {
                return;
            }

            // Ensure that the data source is enabled.
            Assert.isTrue(vo.isEnabled());

            // Create and start the runtime version of the publisher.
            PublisherRT<?> publisher = vo.createPublisherRT();
            publisher.initialize();

            // Add it to the list of running publishers.
            runningPublishers.add(publisher);
        }
    }

    private void stopPublisher(int id) {
        synchronized (runningPublishers) {
            PublisherRT<?> publisher = getRunningPublisher(id);
            if (publisher == null) {
                return;
            }

            runningPublishers.remove(publisher);
            publisher.terminate();
            publisher.joinTermination();
        }
    }

    //
    //
    // Point links
    //
    private PointLinkRT getRunningPointLink(int pointLinkId) {
        for (PointLinkRT pointLink : pointLinks) {
            if (pointLink.getId() == pointLinkId) {
                return pointLink;
            }
        }
        return null;
    }

    public boolean isPointLinkRunning(int pointLinkId) {
        return getRunningPointLink(pointLinkId) != null;
    }

    public void deletePointLink(int pointLinkId) {
        stopPointLink(pointLinkId);
        new PointLinkDao().deletePointLink(pointLinkId);
    }

    public void savePointLink(PointLinkVO vo) {
        // If the point link is running, stop it.
        stopPointLink(vo.getId());

        new PointLinkDao().savePointLink(vo);

        // If the point link is enabled, start it.
        if (!vo.isDisabled()) {
            startPointLink(vo);
        }
    }

    private void startPointLink(PointLinkVO vo) {
        synchronized (pointLinks) {
            // If the point link is already running, just quit.
            if (isPointLinkRunning(vo.getId())) {
                return;
            }

            // Ensure that the point link is enabled.
            Assert.isTrue(!vo.isDisabled());

            // Create and start the runtime version of the point link.
            PointLinkRT pointLink = new PointLinkRT(vo);
            pointLink.initialize();

            // Add it to the list of running point links.
            pointLinks.add(pointLink);
        }
    }

    private void stopPointLink(int id) {
        synchronized (pointLinks) {
            PointLinkRT pointLink = getRunningPointLink(id);
            if (pointLink == null) {
                return;
            }

            pointLinks.remove(pointLink);
            pointLink.terminate();
        }
    }

    //
    //
    // Maintenance events
    //
    public MaintenanceEventRT getRunningMaintenanceEvent(int id) {
        for (MaintenanceEventRT rt : maintenanceEvents) {
            if (rt.getVo().getId() == id) {
                return rt;
            }
        }
        return null;
    }

    public boolean isActiveMaintenanceEvent(int dataSourceId) {
        for (MaintenanceEventRT rt : maintenanceEvents) {
            if (rt.getVo().getDataSourceId() == dataSourceId
                    && rt.isEventActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMaintenanceEventRunning(int id) {
        return getRunningMaintenanceEvent(id) != null;
    }

    public void deleteMaintenanceEvent(int id) {
        stopMaintenanceEvent(id);
        new MaintenanceEventDao().deleteMaintenanceEvent(id);
    }

    public void saveMaintenanceEvent(MaintenanceEventVO vo) {
        // If the maintenance event is running, stop it.
        stopMaintenanceEvent(vo.getId());

        new MaintenanceEventDao().saveMaintenanceEvent(vo);

        // If the maintenance event is enabled, start it.
        if (!vo.isDisabled()) {
            startMaintenanceEvent(vo);
        }
    }

    private void startMaintenanceEvent(MaintenanceEventVO vo) {
        synchronized (maintenanceEvents) {
            // If the maintenance event is already running, just quit.
            if (isMaintenanceEventRunning(vo.getId())) {
                return;
            }

            // Ensure that the maintenance event is enabled.
            Assert.isTrue(!vo.isDisabled());

            // Create and start the runtime version of the maintenance event.
            MaintenanceEventRT rt = new MaintenanceEventRT(vo);
            rt.initialize();

            // Add it to the list of running maintenance events.
            maintenanceEvents.add(rt);
        }
    }

    private void stopMaintenanceEvent(int id) {
        synchronized (maintenanceEvents) {
            MaintenanceEventRT rt = getRunningMaintenanceEvent(id);
            if (rt == null) {
                return;
            }

            maintenanceEvents.remove(rt);
            rt.terminate();
        }
    }

}
 
