package com.serotonin.mango.web.dwr.beans;

import com.serotonin.mango.rt.event.AlarmLevels;
import java.util.Date;


public class EventExportDefinition {
    private final int eventId;
    private final int eventSourceType;
    private final String status;
    private final AlarmLevels alarmLevel;
    private final String[] keywords;
    private final Date dateFrom;
    private final Date dateTo;
    private final int userId;

    public EventExportDefinition(int eventId, int eventSourceType, String status, AlarmLevels alarmLevel, String[] keywords,
            Date dateFrom, Date dateTo, int userId) {
        this.eventId = eventId;
        this.eventSourceType = eventSourceType;
        this.status = status;
        this.alarmLevel = alarmLevel;
        this.keywords = keywords;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public int getEventSourceType() {
        return eventSourceType;
    }

    public String getStatus() {
        return status;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public int getUserId() {
        return userId;
    }
}
