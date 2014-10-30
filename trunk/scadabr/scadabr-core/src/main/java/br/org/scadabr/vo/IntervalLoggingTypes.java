/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo;

import br.org.scadabr.utils.i18n.LocalizableEnum;

/**
 *
 * @author aploese
 */
public enum IntervalLoggingTypes implements LocalizableEnum<IntervalLoggingTypes>{

        INSTANT(1, "pointEdit.logging.valueType.instant"),
        MAXIMUM(2, "pointEdit.logging.valueType.maximum"),
        MINIMUM(3, "pointEdit.logging.valueType.minimum"),
        AVERAGE(4, "pointEdit.logging.valueType.average");

    public final int mangoDbId;
    public final String i18nKey;

    private IntervalLoggingTypes(int mangoDbId, String i18nKey) {
        this.mangoDbId = mangoDbId;
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

    public static IntervalLoggingTypes fromMangoDbId(int mangoDbId) {
        switch (mangoDbId) {
            case 1:
                return IntervalLoggingTypes.INSTANT;
            case 2:
                return IntervalLoggingTypes.MAXIMUM;
            case 3:
                return IntervalLoggingTypes.MINIMUM;
            case 4:
                return IntervalLoggingTypes.AVERAGE;
            default:
                throw new RuntimeException("Cant get LoggingTypes from mangoDbId: " + mangoDbId);
        }
    }
}
