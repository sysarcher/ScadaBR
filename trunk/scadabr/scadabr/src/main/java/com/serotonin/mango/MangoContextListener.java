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
/* modified for NORD Electric by MCA Sistemas */
package com.serotonin.mango;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.timer.CronTimerPool;
import br.org.scadabr.timer.cron.CronExpression;
import com.serotonin.mango.db.dao.ReportDao;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataSource.http.HttpReceiverMulticaster;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.rt.maint.BackgroundProcessing;
import com.serotonin.mango.rt.maint.DataPurge;
import com.serotonin.mango.rt.maint.WorkItemMonitor;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.view.DynamicImage;
import com.serotonin.mango.view.ImageSet;
import com.serotonin.mango.view.ViewGraphic;
import com.serotonin.mango.view.ViewGraphicLoader;
import com.serotonin.mango.vo.report.ReportTask;
import com.serotonin.mango.vo.report.ReportVO;
import com.serotonin.mango.web.ContextWrapper;
import br.org.scadabr.vo.event.type.SystemEventSource;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import java.text.ParseException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import com.serotonin.mango.db.DatabaseAccessFactory;

public class MangoContextListener implements ServletContextListener {

    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_CORE);

    @Inject
    private SystemSettingsDao systemSettingsDao;
    @Inject
    private DatabaseAccessFactory databaseAccessFactory;
    @Inject
    private RuntimeManager runtimeManager;
    @Inject
    private EventManager eventManager;

    @Override
    public void contextInitialized(ServletContextEvent evt) {
        LOG.info("Mango context starting");

        // Get a handle on the context.
        ServletContext ctx = evt.getServletContext();

        // Create the common reference to the context
        Common.ctx = new ContextWrapper(ctx);

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        // Once the threadpool was shut down no its dead, so create them new here....
        Common.dataSourcePool = new CronTimerPool(2, 5, 30, TimeUnit.SECONDS);
        Common.systemCronPool = new CronTimerPool(2, 5, 30, TimeUnit.SECONDS);
        Common.eventCronPool = new CronTimerPool(2, 5, 30, TimeUnit.SECONDS);

        // Create all the stuff we need.
//TODO        freemarkerInitialize(ctx);
//TODO        imageSetInitialize(ctx);
        databaseAccessFactory.startDB();

        // Check if the known servlet context path has changed.
        String knownContextPath = systemSettingsDao.getValue(systemSettingsDao.SERVLET_CONTEXT_PATH);
        if (knownContextPath != null) {
            String contextPath = ctx.getContextPath();
            if (!Objects.equals(knownContextPath, contextPath)) {
                LOG.log(Level.SEVERE, "Mango''s known servlet context path has changed from {0} to {1}. Are there two instances of Mango running?", new Object[]{knownContextPath, contextPath});
            }
        }

        systemSettingsDao.setValue(SystemSettingsDao.SERVLET_CONTEXT_PATH, ctx.getContextPath());

        utilitiesInitialize(ctx);
        eventManagerInitialize(ctx);
        runtimeManagerInitialize(ctx);
        reportsInitialize();
        maintenanceInitialize();
        // Notify the event manager of the startup.
        new SystemEventType(SystemEventSource.SYSTEM_STARTUP).fire("event.system.startup");

        LOG.info("Mango context started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent evt) {
        LOG.info("Mango context terminating");

        if (eventManager != null) {
            // Notify the event manager of the shutdown.
            new SystemEventType(SystemEventSource.SYSTEM_SHUTDOWN).fire("event.system.shutdown");
        }
        Logger.getLogger(MangoContextListener.class.getName()).log(Level.INFO, "Shutdown Event created");

        // Get a handle on the context.
        ContextWrapper ctx = new ContextWrapper(evt.getServletContext());

        // Stop everything.
        terminateRuntimeManager();
        Logger.getLogger(MangoContextListener.class.getName()).log(Level.INFO, "RuntimeManger terminated");
        terminateEventManager();
        Logger.getLogger(MangoContextListener.class.getName()).log(Level.INFO, "EventManager terminated");
        utilitiesTerminate(ctx);
        Logger.getLogger(MangoContextListener.class.getName()).log(Level.INFO, "utilitues terminated");

        //TODO move to Common
        Logger.getLogger(MangoContextListener.class.getName()).log(Level.INFO, "utilitues terminated");
        LOG.log(Level.INFO, "Shutdown dataSourcePool active: {0} queue size: {1}", new Object[]{Common.dataSourcePool.getActiveCount(), Common.dataSourcePool.getQueueSize()});
        Common.dataSourcePool.shutdown();
        LOG.log(Level.INFO, "Shutdown eventCronPool active: {0} queue size: {1}", new Object[]{Common.eventCronPool.getActiveCount(), Common.eventCronPool.getQueueSize()});
        Common.eventCronPool.shutdown();
        LOG.log(Level.INFO, "Shutdown systemCronPool active: {0} queue size: {1}", new Object[]{Common.systemCronPool.getActiveCount(), Common.systemCronPool.getQueueSize()});
        Common.systemCronPool.shutdown();

        try {
            if (Common.dataSourcePool.awaitTermination(10, TimeUnit.SECONDS)) {
                LOG.info("dataSourcePool terminated");
            } else {
                LOG.log(Level.SEVERE, "dataSourcePool termination timeout active: {0} queue size: {1}", new Object[]{Common.dataSourcePool.getActiveCount(), Common.dataSourcePool.getQueueSize()});
            }
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "dataSourcePool termination interrupted exception active: {0} queue size: {1}", new Object[]{Common.dataSourcePool.getActiveCount(), Common.dataSourcePool.getQueueSize()});
        }

        try {
            if (Common.eventCronPool.awaitTermination(10, TimeUnit.SECONDS)) {
                LOG.info("eventCronPool terminated");
            } else {
                LOG.log(Level.SEVERE, "eventCronPool termination timeout active: {0} queue size: {1}", new Object[]{Common.eventCronPool.getActiveCount(), Common.eventCronPool.getQueueSize()});
            }
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "eventCronPool termination interrupted exception active: {0} queue size: {1}", new Object[]{Common.eventCronPool.getActiveCount(), Common.eventCronPool.getQueueSize()});
        }

        try {
            if (Common.systemCronPool.awaitTermination(10, TimeUnit.SECONDS)) {
                LOG.info("systemCronPool terminated");
            } else {
                LOG.log(Level.SEVERE, "systemCronPool termination timeout active: {0} queue size: {1}", new Object[]{Common.systemCronPool.getActiveCount(), Common.systemCronPool.getQueueSize()});
            }
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "systemCronPool termination interrupted exception active: {0} queue size: {1}", new Object[]{Common.systemCronPool.getActiveCount(), Common.systemCronPool.getQueueSize()});
        }

        Common.dataSourcePool = null;
        Common.eventCronPool = null;
        Common.systemCronPool = null;

        if (databaseAccessFactory != null) {
            databaseAccessFactory.stopDB();
            databaseAccessFactory = null;
            LOG.info("Database terminated");
        }

        Common.ctx = null;

        LOG.info("Mango context terminated");
    }

    //
    //
    // Utilities.
    //
    private void utilitiesInitialize(ServletContext ctx) {
        // Except for the BackgroundProcessing process, which is a thread of its
        // own and manages itself by using
        // a blocking queue.
        BackgroundProcessing bp = new BackgroundProcessing();
        bp.initialize();
        ctx.setAttribute(Common.ContextKeys.BACKGROUND_PROCESSING, bp);

        // HTTP receiver multicaster
        ctx.setAttribute(Common.ContextKeys.HTTP_RECEIVER_MULTICASTER, new HttpReceiverMulticaster());

    }

    private void utilitiesTerminate(ContextWrapper ctx) {
        BackgroundProcessing bp = ctx.getBackgroundProcessing();
        if (bp != null) {
            bp.terminate();
            bp.joinTermination();
        }
    }

    //
    //
    // Event manager
    //
    @Deprecated
    private void eventManagerInitialize(ServletContext ctx) {
        ctx.setAttribute(Common.ContextKeys.EVENT_MANAGER, eventManager);
        eventManager.initialize();
    }

    private void terminateEventManager() {
        if (eventManager != null) {
            eventManager.terminate();
            eventManager.joinTermination();
        }
    }

    //
    //
    // Runtime manager
    //
    private void runtimeManagerInitialize(ServletContext ctx) {

        File safeFile = null;
        // Check for safe mode.
        try {
            String s = ctx.getRealPath("/SAFE");
            if (s != null) {
                safeFile = new File(s);
            }
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "Save file ", t);
        }
        boolean safe = false;
        if (safeFile != null) {
            if (safeFile.exists() && safeFile.isFile()) {
                // Indicate that we're in safe mode.
                StringBuilder sb = new StringBuilder();
                sb.append("\r\n");
                sb.append("*********************************************************\r\n");
                sb.append("*                    NOTE                               *\r\n");
                sb.append("*********************************************************\r\n");
                sb.append("* ScadaBR is starting in safe mode. All data sources,   *\r\n");
                sb.append("* point links, scheduled events, compound events, and   *\r\n");
                sb.append("* publishers will be disabled. To disable safe mode,    *\r\n");
                sb.append("* remove the SAFE file from the ScadaBR application     *\r\n");
                sb.append("* directory.                                            *\r\n");
                sb.append("*                                                       *\r\n");
                sb.append("* To find all objects that were automatically disabled, *\r\n");
                sb.append("* search for Audit Events on the alarms page.           *\r\n");
                sb.append("*********************************************************");
                LOG.warning(sb.toString());
                safe = true;
            }
        }
        try {
            if (safe) {
                BackgroundContext.set("common.safeMode");
            }
            runtimeManager.initialize(safe);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "RuntimeManager initialization failure", e);
        } finally {
            if (safe) {
                BackgroundContext.remove();
            }
        }
    }

    private void terminateRuntimeManager() {
        if (runtimeManager != null) {
            runtimeManager.terminate();
            runtimeManager.joinTermination();
        }
    }

    //
    //
    // Image sets
    //
    private void imageSetInitialize(ServletContext ctx) {
        ViewGraphicLoader loader = new ViewGraphicLoader();
        List<ImageSet> imageSets = new ArrayList<>();
        List<DynamicImage> dynamicImages = new ArrayList<>();

        for (ViewGraphic g : loader.loadViewGraphics(ctx.getRealPath("/"))) {
            if (g.isImageSet()) {
                imageSets.add((ImageSet) g);
            } else if (g.isDynamicImage()) {
                dynamicImages.add((DynamicImage) g);
            } else {
                throw new ShouldNeverHappenException(
                        "Unknown view graphic type");
            }
        }

        ctx.setAttribute(Common.ContextKeys.IMAGE_SETS, imageSets);
        ctx.setAttribute(Common.ContextKeys.DYNAMIC_IMAGES, dynamicImages);
    }

    //
    //
    // Freemarker
    //
    private void freemarkerInitialize(ServletContext ctx) {
        Configuration cfg = new Configuration();
        try {
            List<TemplateLoader> loaders = new ArrayList<>();

            // Add the override template dir
            try {
                loaders.add(new FileTemplateLoader(new File(ctx
                        .getRealPath("/WEB-INF/ftl-override"))));
            } catch (FileNotFoundException e) {
                // ignore
            }

            // Add the default template dir
            loaders.add(new FileTemplateLoader(new File(ctx
                    .getRealPath("/WEB-INF/ftl"))));

            cfg.setTemplateLoader(new MultiTemplateLoader(loaders
                    .toArray(new TemplateLoader[loaders.size()])));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Exception defining Freemarker template directories", e);
        }
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        ctx.setAttribute(Common.ContextKeys.FREEMARKER_CONFIG, cfg);
    }

    //
    //
    // Reports
    //
    private void reportsInitialize() {
        List<ReportVO> reports = ReportDao.getInstance().getReports();
        for (ReportVO report : reports) {
            try {
                ReportTask.scheduleReportJob(report);
            } catch (ShouldNeverHappenException e) {
                // Don't stop the startup if there is an error. Just log it.
                LOG.log(Level.SEVERE, "Error starting report " + report.getName(), e);
            }
        }
    }

    //
    //
    // Maintenance processes
    //
    private void maintenanceInitialize() {
        // Processes are scheduled in the timer, so they are canceled when it
        // stops.
        try {
            DataPurge.DataPurgeTask dpt = new DataPurge.DataPurgeTask("0 0 */5 * * * * *", CronExpression.TIMEZONE_UTC);
            System.err.println("CONTEXT DATA PURGE");
            Common.systemCronPool.schedule(dpt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // The version checking job reschedules itself after each execution so
        // that requests from the various Mango
        // instances even out over time.

        //TODO ask serotoninsoftware for new versions ??? Not anymore ;-) VersionCheck.start("0 0 0 0 * * * *");
        WorkItemMonitor.start();

        // MemoryCheck.start();
    }
}
