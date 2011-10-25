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
package com.serotonin.mango.web.dwr;

import freemarker.template.TemplateException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.mail.internet.AddressException;
import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.InvalidArgumentException;
import com.serotonin.mango.Common;
import com.serotonin.mango.SysProperties;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.rt.maint.DataPurge;
import com.serotonin.mango.rt.maint.VersionCheck;
import com.serotonin.mango.rt.maint.work.EmailWorkItem;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.bean.PointHistoryCount;
import com.serotonin.mango.web.email.MangoEmailContent;
import com.serotonin.util.ColorUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.dwr.MethodFilter;
import com.serotonin.web.i18n.I18NUtils;
import com.serotonin.web.i18n.LocalizableMessage;

public class SystemSettingsDwr extends BaseDwr {

    @Autowired
    private SystemSettingsDao systemSettingsDao;
    @Autowired
    private AuditEventType auditEventType;
    @Autowired
    private SystemEventType systemEventType;
    @Autowired
    private VersionCheck versionCheck;
    @Autowired
    private RuntimeManager runtimeManager;

    @MethodFilter
    public Map<String, Object> getSettings() {
        permissions.ensureAdmin();
        Map<String, Object> settings = new HashMap();

        for (SysProperties prop : SysProperties.values()) {
            settings.put(prop.key, systemSettingsDao.getValue(prop));
        }

        // System event types
        settings.put("systemEventTypes", systemEventType.getSystemEventTypes());

        // System event types
        settings.put("auditEventTypes", auditEventType.getAuditEventTypes());

        return settings;
    }

    @MethodFilter
    public Map<String, Object> getDatabaseStatistics() {
        permissions.ensureAdmin();
        Map<String, Object> data = new HashMap();

        // Point history counts.
        List<PointHistoryCount> counts = new DataPointDao().getTopPointHistoryCounts();
        int sum = 0;
        for (PointHistoryCount c : counts) {
            sum += c.getCount();
        }

        data.put("historyCount", sum);
        data.put("topPoints", counts);
        data.put("eventCount", new EventDao().getEventCount());

        return data;
    }

    @MethodFilter
    public void saveEmailSettings(String host, int port, String from, String name, boolean auth, String username,
            String password, boolean tls, int contentType) {
        permissions.ensureAdmin();
        systemSettingsDao.setValue(SysProperties.EMAIL_SMTP_HOST, host);
        systemSettingsDao.setIntValue(SysProperties.EMAIL_SMTP_PORT, port);
        systemSettingsDao.setValue(SysProperties.EMAIL_FROM_ADDRESS, from);
        systemSettingsDao.setValue(SysProperties.EMAIL_FROM_NAME, name);
        systemSettingsDao.setBooleanValue(SysProperties.EMAIL_AUTHORIZATION, auth);
        systemSettingsDao.setValue(SysProperties.EMAIL_SMTP_USERNAME, username);
        systemSettingsDao.setValue(SysProperties.EMAIL_SMTP_PASSWORD, password);
        systemSettingsDao.setBooleanValue(SysProperties.EMAIL_TLS, tls);
        systemSettingsDao.setIntValue(SysProperties.EMAIL_CONTENT_TYPE, contentType);
    }

    @MethodFilter
    public Map<String, Object> sendTestEmail(String host, int port, String from, String name, boolean auth,
            String username, String password, boolean tls, int contentType) {
        permissions.ensureAdmin();

        // Save the settings
        saveEmailSettings(host, port, from, name, auth, username, password, tls, contentType);

        // Get the web context information
        User user = common.getUser();

        Map<String, Object> result = new HashMap();
        try {
            ResourceBundle bundle = getResourceBundle();
            Map<String, Object> model = new HashMap();
            model.put("message", new LocalizableMessage("systemSettings.testEmail"));
            MangoEmailContent cnt = new MangoEmailContent("testEmail", model, bundle, I18NUtils.getMessage(bundle,
                    "ftl.testEmail"), Common.UTF8);
            EmailWorkItem.queueEmail(user.getEmail(), cnt);
            result.put("message", new LocalizableMessage("common.testEmailSent", user.getEmail()));
        } catch (TemplateException | IOException | AddressException e) {
            result.put("exception", e.getMessage());
        }
        return result;
    }

    @MethodFilter
    public void saveSystemEventAlarmLevels(Map<Integer, AlarmLevels> eventAlarmLevels) {
        permissions.ensureAdmin();
        for (Integer eventId : eventAlarmLevels.keySet()) {
            systemEventType.setEventTypeAlarmLevel(eventId, eventAlarmLevels.get(eventId));
        }
    }

    @MethodFilter
    public void saveAuditEventAlarmLevels(Map<Integer, AlarmLevels> eventAlarmLevels) {
        permissions.ensureAdmin();
        for (Integer eventId : eventAlarmLevels.keySet()) {
            auditEventType.setEventTypeAlarmLevel(eventId, eventAlarmLevels.get(eventId));
        }
    }

    @MethodFilter
    public void saveHttpSettings(boolean useProxy, String host, int port, String username, String password) {
        permissions.ensureAdmin();
        systemSettingsDao.setBooleanValue(SysProperties.HTTP_CLIENT_USE_PROXY, useProxy);
        systemSettingsDao.setValue(SysProperties.HTTP_CLIENT_PROXY_SERVER, host);
        systemSettingsDao.setIntValue(SysProperties.HTTP_CLIENT_PROXY_PORT, port);
        systemSettingsDao.setValue(SysProperties.HTTP_CLIENT_PROXY_USERNAME, username);
        systemSettingsDao.setValue(SysProperties.HTTP_CLIENT_PROXY_PASSWORD, password);
    }

    @MethodFilter
    public void saveMiscSettings(int eventPurgePeriodType, int eventPurgePeriods, int reportPurgePeriodType,
            int reportPurgePeriods, int uiPerformance, boolean groveLogging, int futureDateLimitPeriodType,
            int futureDateLimitPeriods) {
        permissions.ensureAdmin();
        systemSettingsDao.setIntValue(SysProperties.EVENT_PURGE_PERIOD_TYPE, eventPurgePeriodType);
        systemSettingsDao.setIntValue(SysProperties.EVENT_PURGE_PERIODS, eventPurgePeriods);
        systemSettingsDao.setIntValue(SysProperties.REPORT_PURGE_PERIOD_TYPE, reportPurgePeriodType);
        systemSettingsDao.setIntValue(SysProperties.REPORT_PURGE_PERIODS, reportPurgePeriods);
        systemSettingsDao.setIntValue(SysProperties.UI_PERFORAMANCE, uiPerformance);
        systemSettingsDao.setBooleanValue(SysProperties.GROVE_LOGGING, groveLogging);
        systemSettingsDao.setIntValue(SysProperties.FUTURE_DATE_LIMIT_PERIOD_TYPE, futureDateLimitPeriodType);
        systemSettingsDao.setIntValue(SysProperties.FUTURE_DATE_LIMIT_PERIODS, futureDateLimitPeriods);
    }

    @MethodFilter
    public DwrResponseI18n saveColourSettings(String chartBackgroundColour, String plotBackgroundColour,
            String plotGridlineColour) {
        permissions.ensureAdmin();

        DwrResponseI18n response = new DwrResponseI18n();

        try {
            ColorUtils.toColor(chartBackgroundColour);
        } catch (InvalidArgumentException e) {
            response.addContextualMessage(SysProperties.CHART_BACKGROUND_COLOUR.key,
                    "systemSettings.validation.invalidColour");
        }

        try {
            ColorUtils.toColor(plotBackgroundColour);
        } catch (InvalidArgumentException e) {
            response.addContextualMessage(SysProperties.PLOT_BACKGROUND_COLOUR.key,
                    "systemSettings.validation.invalidColour");
        }

        try {
            ColorUtils.toColor(plotGridlineColour);
        } catch (InvalidArgumentException e) {
            response.addContextualMessage(SysProperties.PLOT_GRIDLINE_COLOUR.key,
                    "systemSettings.validation.invalidColour");
        }

        if (!response.getHasMessages()) {
            systemSettingsDao.setValue(SysProperties.CHART_BACKGROUND_COLOUR, chartBackgroundColour);
            systemSettingsDao.setValue(SysProperties.PLOT_BACKGROUND_COLOUR, plotBackgroundColour);
            systemSettingsDao.setValue(SysProperties.PLOT_GRIDLINE_COLOUR, plotGridlineColour);
        }

        return response;
    }

    @MethodFilter
    public void saveInfoSettings(String newVersionNotificationLevel, String instanceDescription) {
        permissions.ensureAdmin();
        systemSettingsDao.setValue(SysProperties.NEW_VERSION_NOTIFICATION_LEVEL, newVersionNotificationLevel);
        systemSettingsDao.setValue(SysProperties.INSTANCE_DESCRIPTION, instanceDescription);
    }

    @MethodFilter
    public String newVersionCheck(String newVersionNotificationLevel) {
        permissions.ensureAdmin();
        try {
            return getMessage(versionCheck.newVersionCheck(newVersionNotificationLevel));
        } catch (SocketTimeoutException e) {
            return getMessage("systemSettings.versionCheck1");
        } catch (Exception e) {
            return getMessage(new LocalizableMessage("systemSettings.versionCheck2", e.getClass().getName(),
                    e.getMessage()));
        }
    }

    @MethodFilter
    public void saveLanguageSettings(String language) {
        permissions.ensureAdmin();
        systemSettingsDao.setValue(SysProperties.LANGUAGE, language);
        common.setSystemLanguage(language);
    }

    @MethodFilter
    public void purgeNow() {
        permissions.ensureAdmin();
        DataPurge dataPurge = new DataPurge();
        dataPurge.execute(System.currentTimeMillis());
    }

    @MethodFilter
    public LocalizableMessage purgeAllData() {
        permissions.ensureAdmin();
        long cnt = runtimeManager.purgeDataPointValues();
        return new LocalizableMessage("systemSettings.purgeDataComplete", cnt);
    }
}
