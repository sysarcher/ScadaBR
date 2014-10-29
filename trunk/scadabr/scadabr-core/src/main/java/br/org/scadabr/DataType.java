package br.org.scadabr;

import br.org.scadabr.i18n.LocalizableMessage;
import java.util.Collection;

//TODO Make enum out of this ...
public enum DataType implements LocalizableMessage {

    UNKNOWN(0, "common.unknown"),
    BINARY(1, "common.dataTypes.binary"),
    MULTISTATE(2, "common.dataTypes.multistate"),
    NUMERIC(3, "common.dataTypes.numeric"),
    ALPHANUMERIC(4, "common.dataTypes.alphanumeric"),
    IMAGE(5, "common.dataTypes.image");
    private final String i18nKey;

    private DataType(int ord, String i18nKey) {
        this.i18nKey = i18nKey;
        if (this.ordinal() != ord) {
            throw new RuntimeException("Alarmlevels Ordinal does not match: " + this.name());
        }
    }

    @Deprecated //For JSON Export ...
    public static String nameValues() {
        final DataType[] values = values();
        StringBuilder result = new StringBuilder();
        for (DataType value : values) {
            result.append(value.getName());
            result.append(" ");
        }
        return result.toString();
    }

    @Deprecated //For JSON Export ...
    public static String nameValues(Collection<DataType> dt) {
        StringBuilder result = new StringBuilder();
        for (DataType value : dt) {
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

    public static DataType valueOf(int ordinal) {
        switch (ordinal) {
            case 0:
                return UNKNOWN;
            case 1:
                return BINARY;
            case 2:
                return MULTISTATE;
            case 3:
                return NUMERIC;
            case 4:
                return ALPHANUMERIC;
            case 5:
                return IMAGE;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object[] getArgs() {
        return null;
    }

}
