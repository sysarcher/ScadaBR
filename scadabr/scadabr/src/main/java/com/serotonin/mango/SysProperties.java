/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango;

import java.awt.Color;

/** list of well known keys */
/**
 *
 * @author aploese
 */
public enum SysProperties {

    /** Product version */
    PRODUCT_VERSION("productVersion", "NOT SET YET"), 
    // Servlet context name
    SERVLET_CONTEXT_PATH("servletContextPath"), 
    // Email settings
    EMAIL_SMTP_HOST("emailSmtpHost"),
    EMAIL_SMTP_PORT("emailSmtpPort", 25),
    EMAIL_FROM_ADDRESS("emailFromAddress"),
    EMAIL_FROM_NAME("emailFromName", "Mango M2M"),
    EMAIL_AUTHORIZATION("emailAuthorization"),
    EMAIL_SMTP_USERNAME("emailSmtpUsername"),
    EMAIL_SMTP_PASSWORD("emailSmtpPassword"),
    EMAIL_TLS("emailTls", ""), 
    EMAIL_CONTENT_TYPE("emailContentType"), 
    // Event purging
    EVENT_PURGE_PERIOD_TYPE("eventPurgePeriodType", Common.TIME_PERIOD_CODES.getCode(Common.TimePeriods.YEARS)), 
    EVENT_PURGE_PERIODS("eventPurgePeriods", 1), 
    // Report purging
    REPORT_PURGE_PERIOD_TYPE("reportPurgePeriodType", Common.TIME_PERIOD_CODES.getCode(Common.TimePeriods.MONTHS)), 
    REPORT_PURGE_PERIODS("reportPurgePeriods", 1), 
    // HTTP Client configuration
    HTTP_CLIENT_USE_PROXY("httpClientUseProxy"), 
    HTTP_CLIENT_PROXY_SERVER("httpClientProxyServer"), 
    HTTP_CLIENT_PROXY_PORT("httpClientProxyPort", -1), 
    HTTP_CLIENT_PROXY_USERNAME("httpClientProxyUsername"), 
    HTTP_CLIENT_PROXY_PASSWORD("httpClientProxyPassword"), 
    // New Mango version
    NEW_VERSION_NOTIFICATION_LEVEL("newVersionNotificationLevel", GrooveNotificationLevel.STABLE.name()), 
    // i18n
    LANGUAGE("language", "en"), 
    // Customization
    FILEDATA_PATH("filedata.path", "~/WEB-INF/filedata"), 
    DATASOURCE_DISPLAY_SUFFIX(".display"), 
    HTTPDS_PROLOGUE("httpdsPrologue"), 
    HTTPDS_EPILOGUE("httpdsEpilogue"), 
    UI_PERFORAMANCE("uiPerformance", 2000), 
    GROVE_LOGGING("groveLogging", false), 
    FUTURE_DATE_LIMIT_PERIOD_TYPE("futureDateLimitPeriodType", Common.TIME_PERIOD_CODES.getCode(Common.TimePeriods.HOURS)), 
    FUTURE_DATE_LIMIT_PERIODS("futureDateLimitPeriods", 24), 
    INSTANCE_DESCRIPTION("instanceDescription", "ScadaBR - Powered by Serotonin's Mango M2M"), 
    // Colours
    CHART_BACKGROUND_COLOUR("chartBackgroundColour", Color.WHITE), 
    PLOT_BACKGROUND_COLOUR("plotBackgroundColour", Color.WHITE), 
    PLOT_GRIDLINE_COLOUR("plotGridlineColour", Color.LIGHT_GRAY);
    public final String key;
    public final String defaultValue;

    private SysProperties(String key) {
        this.key = key;
        this.defaultValue = "";
    }
    
    private SysProperties(String key, boolean  defaultValue) {
        this.key = key;
        this.defaultValue = Boolean.toString(defaultValue);
    }
    
    private SysProperties(String key, int defaultValue) {
        this.key = key;
        this.defaultValue = Integer.toString(defaultValue);
    }

    private SysProperties(String key, Color defaultValue) {
        this.key = key;
        this.defaultValue = "0x" +Integer.toHexString(defaultValue.getRGB());
    }
    
    private SysProperties(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
}
