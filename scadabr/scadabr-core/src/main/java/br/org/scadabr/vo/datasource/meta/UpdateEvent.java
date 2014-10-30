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

    public final int mangoDbId;
    private final String i18nKey;
    private final TimePeriods timePeriods;

    private UpdateEvent(int mangoDbId, String i18nKey) {
        this.mangoDbId = mangoDbId;
        this.timePeriods = null;
        this.i18nKey = i18nKey;
    }

    private UpdateEvent(TimePeriods timePeriods, String i18nKey) {
        this.mangoDbId = timePeriods.mangoDbId;
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

    public static UpdateEvent fromMangoDbId(int mangoDbId) {
        switch (mangoDbId) {
            case 0:
                return CONTEXT_UPDATE;
            case 1:
                return MINUTES;
            case 2:
                return HOURS;
            case 3:
                return DAYS;
            case 4:
                return WEEKS;
            case 5:
                return MONTHS;
            case 6:
                return YEARS;
            case 100:
                return CRON;
            default:
                throw new RuntimeException("Cant get UpdateEvents from mangoDbId: " + mangoDbId);

        }
    }
    
    public TimePeriods getTimePeriods() {
        return timePeriods;
    }

}
