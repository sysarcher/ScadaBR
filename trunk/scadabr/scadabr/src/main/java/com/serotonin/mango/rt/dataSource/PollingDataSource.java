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

import br.org.scadabr.ImplementMeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.DataSourceCronTask;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class PollingDataSource extends DataSourceRT {

    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCARABR_DS_RT);

    private final DataSourceVO<?> vo;
    protected List<DataPointRT> dataPoints = new ArrayList<>();
    protected boolean pointListChanged = false;
    private String cronPattern = "0 0 5 * * * * * *"; // Default to 5 minutes just to have something here
    private TimeZone timeZone = CronExpression.TIMEZONE_UTC;
    private DataSourceCronTask timerTask;
    private Thread jobThread;
    private long jobThreadStartTime;

    public PollingDataSource(DataSourceVO<?> vo) {
        super(vo);
        this.vo = vo;
    }

    public void setCronPattern(String cronPattern) {
        this.cronPattern = cronPattern;
    }

    public void collectData(long fireTime) {
        if (jobThread != null) {
            // There is another poll still running, so abort this one.
            LOG.log(Level.WARNING, "{0}: poll at {1} aborted because a previous poll started at {2} is still running", new Object[]{vo.getName(), new Date(fireTime), new Date(jobThreadStartTime)});
            return;
        }

        try {
            jobThread = Thread.currentThread();
            jobThreadStartTime = fireTime;

            // Check if there were changes to the data points list.
            synchronized (pointListChangeLock) {
                updateChangedPoints();
                doPoll(fireTime);
            }
        } catch (Throwable t) {
            System.err.println(t);
        } finally {
            jobThread = null;
        }
    }

    abstract protected void doPoll(long time);

    protected void updateChangedPoints() {
        synchronized (pointListChangeLock) {
            if (addedChangedPoints.size() > 0) {
                // Remove any existing instances of the points.
                dataPoints.removeAll(addedChangedPoints);
                dataPoints.addAll(addedChangedPoints);
                addedChangedPoints.clear();
                pointListChanged = true;
            }
            if (removedPoints.size() > 0) {
                dataPoints.removeAll(removedPoints);
                removedPoints.clear();
                pointListChanged = true;
            }
        }
    }

    //
    //
    // Data source interface
    //
    @Override
    public void beginPolling() {
        try {
            timerTask = new DataSourceCronTask(this, cronPattern, timeZone);
            super.beginPolling();
            Common.dataSourcePool.schedule(timerTask);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void terminate() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        super.terminate();
    }

    @Override
    public void joinTermination() {
        super.joinTermination();

        Thread localThread = jobThread;
        if (localThread != null) {
            try {
                localThread.join(30000); // 30 seconds
            } catch (InterruptedException e) { /* no op */

            }
            if (jobThread != null) {
                throw new ShouldNeverHappenException("Timeout waiting for data source to stop: id=" + getId()
                        + ", type=" + getClass() + ", stackTrace=" + Arrays.toString(localThread.getStackTrace()));
            }
        }
    }

    @Deprecated
    protected void setPollingPeriod(int updatePeriodType, int updatePeriods, boolean quantize) {
        // Set cronpattern from this
        throw new ImplementMeException();
    }

}
