/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.rt;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.vo.DataPointVO;

/**
 *
 * @author aploese
 */
public interface IDataPointLiveCycleListener {

    void dataPointEnabled(DataPointRT dataPoint);

    void dataPointDisabled(DataPointRT dataPoint);

    /**
     * the dataPoint is already disabled...
     * 
     * @param dataPoint 
     */
    void dataPointDeleted(DataPointVO dataPoint);
}
