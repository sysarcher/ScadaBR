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
package com.serotonin.mango.db.dao;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import br.org.scadabr.InvalidArgumentException;
import br.org.scadabr.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import br.org.scadabr.util.ColorUtils;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.vo.event.AlarmLevel;
import br.org.scadabr.vo.event.type.AuditEventSource;
import br.org.scadabr.vo.event.type.SystemEventSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Named
public class SystemSettingsDao extends BaseDao {

    // Database schema version
    public static final String DATABASE_SCHEMA_VERSION = "databaseSchemaVersion";

    // Servlet context name
    public static final String SERVLET_CONTEXT_PATH = "servletContextPath";

    // Email settings
    public static final String EMAIL_SMTP_HOST = "emailSmtpHost";
    public static final String EMAIL_SMTP_PORT = "emailSmtpPort";
    public static final String EMAIL_FROM_ADDRESS = "emailFromAddress";
    public static final String EMAIL_FROM_NAME = "emailFromName";
    public static final String EMAIL_AUTHORIZATION = "emailAuthorization";
    public static final String EMAIL_SMTP_USERNAME = "emailSmtpUsername";
    public static final String EMAIL_SMTP_PASSWORD = "emailSmtpPassword";
    public static final String EMAIL_TLS = "emailTls";
    public static final String EMAIL_CONTENT_TYPE = "emailContentType";

    // Event purging
    public static final String EVENT_PURGE_PERIOD_TYPE = "eventPurgePeriodType";
    public static final String EVENT_PURGE_PERIODS = "eventPurgePeriods";

    // Report purging
    public static final String REPORT_PURGE_PERIOD_TYPE = "reportPurgePeriodType";
    public static final String REPORT_PURGE_PERIODS = "reportPurgePeriods";

    // HTTP Client configuration
    public static final String HTTP_CLIENT_USE_PROXY = "httpClientUseProxy";
    public static final String HTTP_CLIENT_PROXY_SERVER = "httpClientProxyServer";
    public static final String HTTP_CLIENT_PROXY_PORT = "httpClientProxyPort";
    public static final String HTTP_CLIENT_PROXY_USERNAME = "httpClientProxyUsername";
    public static final String HTTP_CLIENT_PROXY_PASSWORD = "httpClientProxyPassword";

    // New Mango version
    public static final String NEW_VERSION_NOTIFICATION_LEVEL = "newVersionNotificationLevel";
    public static final String NOTIFICATION_LEVEL_STABLE = "S";
    public static final String NOTIFICATION_LEVEL_RC = "C";
    public static final String NOTIFICATION_LEVEL_BETA = "B";

    // i18n
    public static final String LANGUAGE = "language";

    // Customization
    public static final String FILEDATA_PATH = "filedata.path";
    public static final String DATASOURCE_DISPLAY_SUFFIX = ".display";
    public static final String HTTPDS_PROLOGUE = "httpdsPrologue";
    public static final String HTTPDS_EPILOGUE = "httpdsEpilogue";
    public static final String UI_PERFORAMANCE = "uiPerformance";
    public static final String GROVE_LOGGING = "groveLogging";
    public static final String FUTURE_DATE_LIMIT_PERIODS = "futureDateLimitPeriods";
    public static final String FUTURE_DATE_LIMIT_PERIOD_TYPE = "futureDateLimitPeriodType";
    public static final String INSTANCE_DESCRIPTION = "instanceDescription";

    // Colours
    public static final String CHART_BACKGROUND_COLOUR = "chartBackgroundColour";
    public static final String PLOT_BACKGROUND_COLOUR = "plotBackgroundColour";
    public static final String PLOT_GRIDLINE_COLOUR = "plotGridlineColour";

    
    //
    // /
    // / Static stuff
    // /
    //
    private static final String SYSTEM_EVENT_ALARMLEVEL_PREFIX = "systemEventAlarmLevel";
    private static final String AUDIT_EVENT_ALARMLEVEL_PREFIX = "auditEventAlarmLevel";

    // Value cache 
    //TODO cache real objects???
    private static final Map<String, String> cache = new HashMap<>();
    
//    @PostConstruct // todo if getInstance is removed and no static access use this ...
    @Override
    public void init() {
        super.init();
        for (SystemEventSource s : SystemEventSource.values()) {
            final AlarmLevel l = s.getAlarmLevel();
            if (l != null) {
                // override if avail
                s.setAlarmLevel(l);
            }
        }
    }

    public AlarmLevel getAlarmLevel(SystemEventSource key) {
        String value = getValue(SYSTEM_EVENT_ALARMLEVEL_PREFIX + key.name(), null);
        if (value == null) {
            return null;
        }
        return AlarmLevel.values()[Integer.parseInt(value)];
    }

    public static void setValue(JdbcTemplate ejt, String key, String value) {
        ejt.execute(String.format("insert into systemSettings values ('%s','%s')", key, value));
    }

    public SystemSettingsDao() {
        super();
    }

    public String getValue(String key) {
        return getValue(key, (String) DEFAULT_VALUES.get(key));
    }

    public String getValue(String key, String defaultValue) {
        String result = cache.get(key);
        if (result == null) {
            if (!cache.containsKey(key)) {
                try {
                    //TODOD was BaseDao
                    result = ejt.queryForObject("select settingValue from systemSettings where settingName=?", String.class, key);
                } catch (EmptyResultDataAccessException e) {
                    result = null;
                }
                cache.put(key, result);
                if (result == null) {
                    result = defaultValue;
                }
            } else {
                result = defaultValue;
            }
        }
        return result;
    }

    public int getIntValue(String key) {
        Integer defaultValue = (Integer) DEFAULT_VALUES.get(key);
        if (defaultValue == null) {
            return getIntValue(key, 0);
        }
        return getIntValue(key, defaultValue);
    }

    public TimePeriods getTimePeriodsValue(String key) {
        TimePeriods defaultValue = (TimePeriods) DEFAULT_VALUES.get(key);
        if (defaultValue == null) {
            throw  new ShouldNeverHappenException("No default for: " + key);
        }
        return TimePeriods.fromId(getIntValue(key, defaultValue.getId()));
    }

    public int getIntValue(String key, int defaultValue) {
        String value = getValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanValue(String key) {
        return getBooleanValue(key, false);
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = getValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        return charToBool(value);
    }

    public void setValue(final String key, final String value) {
        // Update the cache
        cache.put(key, value);

        // Update the database
        final JdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(
                            TransactionStatus status) {
                                // Delete any existing value.
                                removeValue(key);

                                // Insert the new value if it's not null.
                                if (value != null) {
                                    ejt2.update(
                                            "insert into systemSettings values (?,?)",
                                            new Object[]{key, value});
                                }
                            }
                });
    }

    public void setIntValue(String key, int value) {
        setValue(key, Integer.toString(value));
    }

    public void setBooleanValue(String key, boolean value) {
        setValue(key, boolToChar(value));
    }

    public void removeValue(String key) {
        // Remove the value from the cache
        cache.remove(key);

        // Reset the cached values too.
        FUTURE_DATE_LIMIT = -1;

        ejt.update("delete from systemSettings where settingName=?",
                new Object[]{key});
    }

    public long getFutureDateLimit() {
        if (FUTURE_DATE_LIMIT == -1) {
            FUTURE_DATE_LIMIT = getTimePeriodsValue(FUTURE_DATE_LIMIT_PERIOD_TYPE).getMillis(getIntValue(FUTURE_DATE_LIMIT_PERIODS));
        }
        return FUTURE_DATE_LIMIT;
    }

    public Color getColour(String key) {
        try {
            return ColorUtils.toColor(getValue(key));
        } catch (InvalidArgumentException e) {
            // Should never happen. Just use the default.
            try {
                return ColorUtils.toColor((String) DEFAULT_VALUES.get(key));
            } catch (InvalidArgumentException e1) {
                // This should definitely never happen
                throw new ShouldNeverHappenException(e1);
            }
        }
    }

    /**
     * Special caching for the future dated values property, which needs high
     * performance.
     */
    private static long FUTURE_DATE_LIMIT = -1;

    public static final Map<String, Object> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(DATABASE_SCHEMA_VERSION, "0.7.0");

        DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_SERVER, "");
        DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_PORT, -1);
        DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_USERNAME, "");
        DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_PASSWORD, "");

        DEFAULT_VALUES.put(EMAIL_SMTP_HOST, "");
        DEFAULT_VALUES.put(EMAIL_SMTP_PORT, 25);
        DEFAULT_VALUES.put(EMAIL_FROM_ADDRESS, "");
        DEFAULT_VALUES.put(EMAIL_SMTP_USERNAME, "");
        DEFAULT_VALUES.put(EMAIL_SMTP_PASSWORD, "");
        DEFAULT_VALUES.put(EMAIL_FROM_NAME, "ScadaBR");

        DEFAULT_VALUES.put(EVENT_PURGE_PERIOD_TYPE, TimePeriods.YEARS);
        DEFAULT_VALUES.put(EVENT_PURGE_PERIODS, 1);

        DEFAULT_VALUES.put(REPORT_PURGE_PERIOD_TYPE, TimePeriods.MONTHS);
        DEFAULT_VALUES.put(REPORT_PURGE_PERIODS, 1);

        DEFAULT_VALUES.put(NEW_VERSION_NOTIFICATION_LEVEL,
                NOTIFICATION_LEVEL_STABLE);

        DEFAULT_VALUES.put(LANGUAGE, "en");

        DEFAULT_VALUES.put(FILEDATA_PATH, "~/WEB-INF/filedata");
        DEFAULT_VALUES.put(HTTPDS_PROLOGUE, "");
        DEFAULT_VALUES.put(HTTPDS_EPILOGUE, "");
        DEFAULT_VALUES.put(UI_PERFORAMANCE, 2000);
        DEFAULT_VALUES.put(GROVE_LOGGING, false);
        DEFAULT_VALUES.put(FUTURE_DATE_LIMIT_PERIODS, 24);
        DEFAULT_VALUES.put(FUTURE_DATE_LIMIT_PERIOD_TYPE,
                TimePeriods.HOURS);
        try {
            DEFAULT_VALUES.put(INSTANCE_DESCRIPTION, String.format("ScadaBR @%s", InetAddress.getLocalHost().getCanonicalHostName()));
        } catch (UnknownHostException uhe) {
            DEFAULT_VALUES.put(INSTANCE_DESCRIPTION, "ScadaBR @Unknown host");
        }

        DEFAULT_VALUES.put(CHART_BACKGROUND_COLOUR, "white");
        DEFAULT_VALUES.put(PLOT_BACKGROUND_COLOUR, "white");
        DEFAULT_VALUES.put(PLOT_GRIDLINE_COLOUR, "silver");
    }

    public void resetDataBase() {
        final JdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(
                            TransactionStatus status) {

                                ejt2.execute("delete from watchLists");
                                ejt2.execute("delete from mangoViews");

                                ejt2.execute("delete from pointEventDetectors");
                                ejt2.execute("delete from compoundEventDetectors");
                                ejt2.execute("delete from scheduledEvents");

                                ejt2.execute("delete from pointLinks");

                                ejt2.execute("delete from events");
                                ejt2.execute("delete from reports");
                                ejt2.execute("delete from pointHierarchy");

                                ejt2.execute("delete from eventHandlers");
                                ejt2.execute("delete from scripts");

                                ejt2.execute("delete from pointValues");
                                ejt2.execute("delete from maintenanceEvents");
                                ejt2.execute("delete from mailingLists");
                                ejt2.execute("delete from compoundEventDetectors");

                                ejt2.execute("delete from users");

                                ejt2.execute("delete from publishers");

                                ejt2.execute("delete from dataPointUsers");
                                ejt2.execute("delete from dataSourceUsers");

                                ejt2.execute("delete from dataPoints");
                                ejt2.execute("delete from dataSources");

                            }
                });

    }

    public void saveAlarmLevel(SystemEventSource key) {
        setValue(SYSTEM_EVENT_ALARMLEVEL_PREFIX + key.name(), Integer.toString(key.getAlarmLevel().getId()));
    }

    public void saveAlarmlevel(AuditEventSource key) {
        setValue(AUDIT_EVENT_ALARMLEVEL_PREFIX + key.name(), Integer.toString(key.getAlarmLevel().getId()));
    }
}
