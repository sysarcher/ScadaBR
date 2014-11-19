/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.datasource.meta;

import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.utils.i18n.LocalizableMessage;

/**
 *
 * @author aploese
 */
public enum UpdateEvent implements LocalizableMessage {

    CONTEXT_UPDATE(0, "dsEdit.meta.event.context"),
    MINUTES(TimePeriods.MINUTES, "dsEdit.meta.event.minute"),
    HOURS(TimePeriods.HOURS, "dsEdit.meta.event.hour"),
    DAYS(TimePeriods.DAYS, "dsEdit.meta.event.day"),
    WEEKS(TimePeriods.WEEKS, "dsEdit.meta.event.week"),
    MONTHS(TimePeriods.MONTHS, "dsEdit.meta.event.month"),
    YEARS(TimePeriods.YEARS, "dsEdit.meta.event.year"),
    CRON(100, "dsEdit.meta.event.cron");

    private final int id;
    private final String i18nKey;
    private final TimePeriods timePeriods;

    private UpdateEvent(int id, String i18nKey) {
        this.id = id;
        this.timePeriods = null;
        this.i18nKey = i18nKey;
    }

    private UpdateEvent(TimePeriods timePeriods, String i18nKey) {
        this.id = timePeriods.getId();
        this.timePeriods = timePeriods;
        this.i18nKey = i18nKey;
    }

    @Override
    public String getI18nKey() {
        return i18nKey;
    }

    @Override
    public Object[] getArgs() {
        return null;
    }
    
    public int getId() {
        return id;
    }

    public static UpdateEvent fromId(int id) {
        switch (id) {
            case 0:
                return CONTEXT_UPDATE;
            case 2:
                return MINUTES;
            case 3:
                return HOURS;
            case 4:
                return DAYS;
            case 5:
                return WEEKS;
            case 6:
                return MONTHS;
            case 7:
                return YEARS;
            case 100:
                return CRON;
            default:
                throw new RuntimeException("Cant get UpdateEvents from mangoDbId: " + id);

        }
    }
    
    public TimePeriods getTimePeriods() {
        return timePeriods;
    }

}
