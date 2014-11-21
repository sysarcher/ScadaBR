/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import br.org.scadabr.DataType;
import br.org.scadabr.logger.LogUtils;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aploese
 */
class BatchWriteBehindEntry {
    private final int pointId;
    private final DataType dataType;
    private final double dvalue;
    private final long time;

    public BatchWriteBehindEntry(int pointId, DataType dataType, double dvalue, long time) {
        this.pointId = pointId;
        this.dataType = dataType;
        this.dvalue = dvalue;
        this.time = time;
    }

    public void writeInto(Object[] params, int index) {
        index *= BatchWriteBehind.POINT_VALUE_INSERT_VALUES_COUNT;
        params[index++] = pointId;
        params[index++] = dataType.mangoDbId;
        params[index++] = dvalue;
        Logger.getLogger(LogUtils.LOGGER_SCADABR_DAO).log(Level.SEVERE, "WRITE TO PARAM TS {0} VALUE: {1}", new Object[]{new Date(time), dvalue});
        params[index++] = time;
    }
    
}
