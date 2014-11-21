/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.rt.datasource;

import br.org.scadabr.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.meta.MetaDataSourceRT;

/**
 *
 * @author aploese
 * @param <T>
 * @param <U>
 */
public abstract class PollingPointLocatorRT<T extends PointLocatorVO, U extends DataSourceRT> extends PointLocatorRT<T> {

    protected DataPointRT dpRT;
    protected U dsRT;
    
    public PollingPointLocatorRT(T vo) {
        super(vo);
    }

    public final U getDsRT() {
        return dsRT;
    }

    public final DataPointRT getDpRT() {
        return dpRT;
    }


    public abstract void doPoll(long scheduledExecutionTime);

    public abstract boolean overrunDetected(long lastExecutionTime, long thisExecutionTime);

    public void start(U dsRT, DataPointRT dpRT) {
        this.dsRT = dsRT;
        this.dpRT = dpRT;
    }
    
    public void stop() {
        this.dsRT = null;
        this.dpRT = null;
    }
    

}
