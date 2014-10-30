package br.org.scadabr.vo.event;

import br.org.scadabr.utils.i18n.LocalizableEnum;

public enum AlarmLevel implements LocalizableEnum<AlarmLevel> {

    //do not reorder!!
    NONE(0, "common.alarmLevel.none"),
    INFORMATION(1, "common.alarmLevel.info"),
    URGENT(2, "common.alarmLevel.urgent"),
    CRITICAL(3, "common.alarmLevel.critical"),
    LIFE_SAFETY(4, "common.alarmLevel.lifeSafety");
    private final String i18nKey;
    public final int mangoDbId;
    
    private AlarmLevel(int mangoDbId, String i18nKey) {
        this.i18nKey = i18nKey;
        this.mangoDbId = mangoDbId;
    }

    @Deprecated //For JSON Export ...
    public static String nameValues() {
        final AlarmLevel[] values = values();
        StringBuilder result = new StringBuilder();
        for (AlarmLevel value : values) {
            result.append(value.getName());
            result.append(" ");
        }
        return result.toString();
    }

    public String getName() {
        return name();
    }

    @Override
    public String getI18nKey() {
        return i18nKey;
    }

    public static AlarmLevel fromMangoDbId(int ordinal) {
        switch (ordinal) {
            case 0:
                return NONE;
            case 1:
                return INFORMATION;
            case 2:
                return URGENT;
            case 3:
                return CRITICAL;
            case 4:
                return LIFE_SAFETY;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object[] getArgs() {
        return null;
    }

    public int toMangoDbId() {
        return mangoDbId;
    }

    public boolean otherIsHigher(AlarmLevel o) {
        return this.ordinal() < o.ordinal();
    }

    public boolean otherIsLower(AlarmLevel o) {
        return this.ordinal() > o.ordinal();
    }

    public boolean meIsHigher(AlarmLevel o) {
        return this.ordinal() > o.ordinal();
    }

    public boolean meIsLower(AlarmLevel o) {
        return this.ordinal() < o.ordinal();
    }

}
