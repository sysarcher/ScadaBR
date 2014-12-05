/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.DoubleDataPointRT;
import com.serotonin.mango.rt.dataImage.DoubleValueTime;

/**
 *
 * @author aploese
 */
public class DoubleDataPointVO extends DataPointVO<DoubleValueTime>{

    @Override
    public DataPointRT<DoubleValueTime> createRT() {
        return new DoubleDataPointRT(this, getPointLocator().createRuntime());
    }
    
}
