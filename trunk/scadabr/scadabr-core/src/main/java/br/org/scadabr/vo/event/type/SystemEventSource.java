/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.event.type;

import br.org.scadabr.utils.i18n.LocalizableEnum;
import br.org.scadabr.vo.event.AlarmLevel;

/**
 *
 * @author aploese
 */
public enum SystemEventSource implements LocalizableEnum<SystemEventSource> {

    SYSTEM_STARTUP(1, "event.system.startup", AlarmLevel.INFORMATION),
    SYSTEM_SHUTDOWN(2, "event.system.shutdown", AlarmLevel.INFORMATION),
    MAX_ALARM_LEVEL_CHANGED(3, "event.system.maxAlarmChanged", AlarmLevel.NONE),
    USER_LOGIN(4, "event.system.userLogin", AlarmLevel.INFORMATION),
    VERSION_CHECK(5, "event.system.versionCheck", AlarmLevel.INFORMATION),
    COMPOUND_DETECTOR_FAILURE(6, "event.system.compound", AlarmLevel.URGENT),
    SET_POINT_HANDLER_FAILURE(7, "event.system.setPoint", AlarmLevel.URGENT),
    EMAIL_SEND_FAILURE(8, "event.system.email", AlarmLevel.INFORMATION),
    POINT_LINK_FAILURE(9, "event.system.pointLink", AlarmLevel.URGENT),
    PROCESS_FAILURE(10, "event.system.process", AlarmLevel.URGENT);
    private final String i18nKey;
    private final int id;
    private final AlarmLevel defaultAlarmLevel;
    private AlarmLevel alarmLevel;

    private SystemEventSource(int id, String i18nKey, AlarmLevel defaultAlarmLevel) {
        this.i18nKey = i18nKey;
        this.id = id;
        this.defaultAlarmLevel = defaultAlarmLevel;
        this.alarmLevel = defaultAlarmLevel;
    }

    @Override
    public String getI18nKey() {
        return i18nKey;
    }

    public static SystemEventSource fromId(int ordinal) {
        switch (ordinal) {
            case 1:
                return SYSTEM_STARTUP;
            case 2:
                return SYSTEM_SHUTDOWN;
            case 3:
                return MAX_ALARM_LEVEL_CHANGED;
            case 4:
                return USER_LOGIN;
            case 5:
                return VERSION_CHECK;
            case 6:
                return COMPOUND_DETECTOR_FAILURE;
            case 7:
                return SET_POINT_HANDLER_FAILURE;
            case 8:
                return EMAIL_SEND_FAILURE;
            case 9:
                return POINT_LINK_FAILURE;
            case 10:
                return PROCESS_FAILURE;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object[] getArgs() {
        return null;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the defaultAlarmLevel
     */
    public AlarmLevel getDefaultAlarmLevel() {
        return defaultAlarmLevel;
    }

    /**
     * @return the alarmLevel
     */
    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

    /**
     * @param alarmLevel the alarmLevel to set
     */
    public void setAlarmLevel(AlarmLevel alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
    
    public boolean isDefaultAlarmlevel() {
        return defaultAlarmLevel == alarmLevel;
    }

}
