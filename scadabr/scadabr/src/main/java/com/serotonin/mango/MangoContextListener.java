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

import br.org.scadabr.api.utils.APIUtils;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.ReportDao;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.dataSource.http.HttpReceiverMulticaster;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.rt.maint.BackgroundProcessing;
import com.serotonin.mango.rt.maint.DataPurge;
import com.serotonin.mango.rt.maint.VersionCheck;
import com.serotonin.mango.rt.maint.WorkItemMonitor;
import com.serotonin.mango.view.DynamicImage;
import com.serotonin.mango.view.ImageSet;
import com.serotonin.mango.view.ViewGraphic;
import com.serotonin.mango.view.ViewGraphicLoader;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.mango.vo.hierarchy.PointHierarchy;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.mango.vo.report.ReportJob;
import com.serotonin.mango.vo.report.ReportVO;
import com.serotonin.mango.web.ContextWrapper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

import org.springframework.stereotype.Service;

@Service
public class MangoContextListener implements ServletContextListener {

    private final static Logger LOG = LoggerFactory.getLogger(MangoContextListener.class);
    @Autowired
    private SystemSettingsDao systemSettingsDao;
    @Autowired
    private EventManager eventManager;

    @Override
    public void contextInitialized(ServletContextEvent evt) {
        LOG.info("Mango context starting");

        // Get a handle on the context.
        ServletContext ctx = evt.getServletContext();

        // Create the common reference to the context
        Common.ctx = new ContextWrapper(ctx);

        // Initialize the timer
        Common.timer.init(new ThreadPoolExecutor(0, 1000, 30L,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));

        // Create all the stuff we need.
        constantsInitialize(ctx);
        freemarkerInitialize(ctx);
        imageSetInitialize(ctx);

        // Check if the known servlet context path has changed.
        String knownContextPath = systemSettingsDao.getValue(SysProperties.SERVLET_CONTEXT_PATH);
        if (knownContextPath != null) {
            String contextPath = ctx.getContextPath();
            if (!StringUtils.isEqual(knownContextPath, contextPath)) {
                LOG.warn("Mango's known servlet context path has changed from "
                        + knownContextPath + " to " + contextPath
                        + ". Are there two instances of Mango running?");
            }
        }
        systemSettingsDao.setValue(
                SysProperties.SERVLET_CONTEXT_PATH, ctx.getContextPath());

        utilitiesInitialize(ctx);
        reportsInitialize();
        maintenanceInitialize();

        // Notify the event manager of the startup.
        eventManager.raiseEvent(new SystemEventType(
                SystemEventType.TYPE_SYSTEM_STARTUP), System.currentTimeMillis(), false, new LocalizableMessage(
                "event.system.startup"));

        LOG.info("Mango context started");

    }

    @Override
    public void contextDestroyed(ServletContextEvent evt) {
        LOG.info("Mango context terminating");

        // Notify the event manager of the shutdown.
        eventManager.raiseEvent(new SystemEventType(
                SystemEventType.TYPE_SYSTEM_SHUTDOWN), System.currentTimeMillis(), false, new LocalizableMessage(
                "event.system.shutdown"));

        // Get a handle on the context.
        ContextWrapper ctx = new ContextWrapper(evt.getServletContext());

        // Stop everything.
        utilitiesTerminate(ctx);

        Common.timer.cancel();
        Common.timer.getExecutorService().shutdown();

        Common.ctx = null;

        LOG.info("Mango context terminated");
    }

    private void dataPointsNameToIdMapping(ServletContext ctx) {
        PointHierarchy pH = new DataPointDao().getPointHierarchy();
        List<DataPointVO> datapoints = new DataPointDao().getDataPoints(null,
                false);

        Map<String, Integer> mapping = new HashMap<String, Integer>();

        for (DataPointVO dataPointVO : datapoints) {
            String completeName = APIUtils.getCompletePath(
                    dataPointVO.getPointFolderId(), pH)
                    + dataPointVO.getName();
            mapping.put(completeName, dataPointVO.getId());
        }

        Common.ctx.getServletContext().setAttribute(
                Common.ContextKeys.DATA_POINTS_NAME_ID_MAPPING, mapping);

    }

    //
    //
    // Constants
    //
    private void constantsInitialize(ServletContext ctx) {
        ctx.setAttribute("constants.Common.NEW_ID", Common.NEW_ID);

        ctx.setAttribute("constants.Permissions.DataPointAccessTypes.NONE",
                Permissions.DataPointAccessTypes.NONE);
        ctx.setAttribute("constants.Permissions.DataPointAccessTypes.READ",
                Permissions.DataPointAccessTypes.READ);
        ctx.setAttribute("constants.Permissions.DataPointAccessTypes.SET",
                Permissions.DataPointAccessTypes.SET);
        ctx.setAttribute(
                "constants.Permissions.DataPointAccessTypes.DATA_SOURCE",
                Permissions.DataPointAccessTypes.DATA_SOURCE);
        ctx.setAttribute("constants.Permissions.DataPointAccessTypes.ADMIN",
                Permissions.DataPointAccessTypes.ADMIN);

        ctx.setAttribute("constants.EventType.EventSources.DATA_POINT",
                EventType.EventSources.DATA_POINT);
        ctx.setAttribute("constants.EventType.EventSources.DATA_SOURCE",
                EventType.EventSources.DATA_SOURCE);
        ctx.setAttribute("constants.EventType.EventSources.SYSTEM",
                EventType.EventSources.SYSTEM);
        ctx.setAttribute("constants.EventType.EventSources.COMPOUND",
                EventType.EventSources.COMPOUND);
        ctx.setAttribute("constants.EventType.EventSources.SCHEDULED",
                EventType.EventSources.SCHEDULED);
        ctx.setAttribute("constants.EventType.EventSources.PUBLISHER",
                EventType.EventSources.PUBLISHER);
        ctx.setAttribute("constants.EventType.EventSources.AUDIT",
                EventType.EventSources.AUDIT);
        ctx.setAttribute("constants.EventType.EventSources.MAINTENANCE",
                EventType.EventSources.MAINTENANCE);
        ctx.setAttribute("constants.SystemEventType.TYPE_SYSTEM_STARTUP",
                SystemEventType.TYPE_SYSTEM_STARTUP);
        ctx.setAttribute("constants.SystemEventType.TYPE_SYSTEM_SHUTDOWN",
                SystemEventType.TYPE_SYSTEM_SHUTDOWN);
        ctx.setAttribute(
                "constants.SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED",
                SystemEventType.TYPE_MAX_ALARM_LEVEL_CHANGED);
        ctx.setAttribute("constants.SystemEventType.TYPE_USER_LOGIN",
                SystemEventType.TYPE_USER_LOGIN);
        ctx.setAttribute("constants.SystemEventType.TYPE_VERSION_CHECK",
                SystemEventType.TYPE_VERSION_CHECK);
        ctx.setAttribute(
                "constants.SystemEventType.TYPE_COMPOUND_DETECTOR_FAILURE",
                SystemEventType.TYPE_COMPOUND_DETECTOR_FAILURE);
        ctx.setAttribute(
                "constants.SystemEventType.TYPE_SET_POINT_HANDLER_FAILURE",
                SystemEventType.TYPE_SET_POINT_HANDLER_FAILURE);
        ctx.setAttribute("constants.SystemEventType.TYPE_EMAIL_SEND_FAILURE",
                SystemEventType.TYPE_EMAIL_SEND_FAILURE);
        ctx.setAttribute("constants.SystemEventType.TYPE_POINT_LINK_FAILURE",
                SystemEventType.TYPE_POINT_LINK_FAILURE);
        ctx.setAttribute("constants.SystemEventType.TYPE_PROCESS_FAILURE",
                SystemEventType.TYPE_PROCESS_FAILURE);

        ctx.setAttribute("constants.AuditEventType.TYPE_DATA_SOURCE",
                AuditEventType.TYPE_DATA_SOURCE);
        ctx.setAttribute("constants.AuditEventType.TYPE_DATA_POINT",
                AuditEventType.TYPE_DATA_POINT);
        ctx.setAttribute("constants.AuditEventType.TYPE_POINT_EVENT_DETECTOR",
                AuditEventType.TYPE_POINT_EVENT_DETECTOR);
        ctx.setAttribute(
                "constants.AuditEventType.TYPE_COMPOUND_EVENT_DETECTOR",
                AuditEventType.TYPE_COMPOUND_EVENT_DETECTOR);
        ctx.setAttribute("constants.AuditEventType.TYPE_SCHEDULED_EVENT",
                AuditEventType.TYPE_SCHEDULED_EVENT);
        ctx.setAttribute("constants.AuditEventType.TYPE_EVENT_HANDLER",
                AuditEventType.TYPE_EVENT_HANDLER);
        ctx.setAttribute("constants.AuditEventType.TYPE_POINT_LINK",
                AuditEventType.TYPE_POINT_LINK);

        ctx.setAttribute("constants.PublisherVO.Types.HTTP_SENDER",
                PublisherVO.Type.HTTP_SENDER.getId());
        ctx.setAttribute("constants.PublisherVO.Types.PACHUBE",
                PublisherVO.Type.PACHUBE.getId());
        ctx.setAttribute("constants.PublisherVO.Types.PERSISTENT",
                PublisherVO.Type.PERSISTENT.getId());

        String[] codes = {"common.access.read", "common.access.set",
            "common.alarmLevel.none", "common.alarmLevel.info",
            "common.alarmLevel.urgent", "common.alarmLevel.critical",
            "common.alarmLevel.lifeSafety", "common.disabled",
            "common.administrator", "common.user", "js.disabledSe",
            "scheduledEvents.se", "js.disabledCed",
            "compoundDetectors.compoundEventDetector",
            "common.disabledToggle", "common.enabledToggle",
            "common.maximize", "common.minimize", "js.help.loading",
            "js.help.error", "js.help.related", "js.help.lastUpdated",
            "common.sendTestEmail", "js.email.noRecipients",
            "js.email.addMailingList", "js.email.addUser",
            "js.email.addAddress", "js.email.noRecipForEmail",
            "js.email.testSent", "events.silence", "events.unsilence",
            "js.disabledPointLink", "pointLinks.pointLink", "header.mute",
            "header.unmute",};

        Map<String, LocalizableMessage> messages = new HashMap<String, LocalizableMessage>();
        for (String code : codes) {
            messages.put(code, new LocalizableMessage(code));
        }
        ctx.setAttribute("clientSideMessages", messages);
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
        ctx.setAttribute(Common.ContextKeys.HTTP_RECEIVER_MULTICASTER,
                new HttpReceiverMulticaster());
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
    // Image sets
    //
    private void imageSetInitialize(ServletContext ctx) {
        ViewGraphicLoader loader = new ViewGraphicLoader();
        List<ImageSet> imageSets = new ArrayList<ImageSet>();
        List<DynamicImage> dynamicImages = new ArrayList<DynamicImage>();

        for (ViewGraphic g : loader.loadViewGraphics(ctx.getRealPath(""))) {
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
            List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();

            // Add the override template dir
            try {
                loaders.add(new FileTemplateLoader(new File(ctx.getRealPath("/WEB-INF/ftl-override"))));
            } catch (FileNotFoundException e) {
                // ignore
            }

            // Add the default template dir
            loaders.add(new FileTemplateLoader(new File(ctx.getRealPath("/WEB-INF/ftl"))));

            cfg.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()])));
        } catch (IOException e) {
            LOG.error("Exception defining Freemarker template directories", e);
        }
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        ctx.setAttribute(Common.ContextKeys.FREEMARKER_CONFIG, cfg);
    }

    //
    //
    // Reports
    //
    private void reportsInitialize() {
        List<ReportVO> reports = new ReportDao().getReports();
        for (ReportVO report : reports) {
            try {
                ReportJob.scheduleReportJob(report);
            } catch (ShouldNeverHappenException e) {
                // Don't stop the startup if there is an error. Just log it.
                LOG.error("Error starting report " + report.getName(), e);
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
        DataPurge.schedule();

        // The version checking job reschedules itself after each execution so
        // that requests from the various Mango
        // instances even out over time.
        VersionCheck.start();
        WorkItemMonitor.start();

        // MemoryCheck.start();
    }
}
