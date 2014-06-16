/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.serotonin.mango.view.chart;

import com.serotonin.mango.DataTypes;

/**
 *
 * @author aploese
 */
public enum ChartType {
    NONE("chartRenderer.none", DataTypes.ALPHANUMERIC, DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC, DataTypes.IMAGE), 
    TABLE("chartRenderer.table", DataTypes.ALPHANUMERIC, DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC), 
    IMAGE("chartRenderer.image", DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC), 
    STATS("chartRenderer.statistics", DataTypes.ALPHANUMERIC, DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC),
    IMAGE_FLIPBOOK("chartRenderer.flipbook", DataTypes.IMAGE);
    
    final String i18nKey;
    final int[] dataTypes;
    
    private ChartType (String i18nKey, int ... dataTypes) {
        this.i18nKey = i18nKey;
        this.dataTypes = dataTypes;
    }
    
    public boolean supports(int dataType) {
        for (int dt: dataTypes) {
            if (dt == dataType) {
                return true;
            }
        }
        return false;
    }
    
}
