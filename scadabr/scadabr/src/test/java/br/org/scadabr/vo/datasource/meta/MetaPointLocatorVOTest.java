/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.datasource.meta;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataSource.meta.MetaDataSourceRT;
import com.serotonin.mango.rt.dataSource.meta.MetaPointLocatorRT;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO;
import com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author aploese
 */
public class MetaPointLocatorVOTest {

    public MetaPointLocatorVOTest() {
    }

    @Before
    public void setUp() {
    }

    
    @Ignore
    @Test
    public void testRunScript() {
        MetaDataSourceVO dsVO = new MetaDataSourceVO();
        MetaPointLocatorVO plVO = dsVO.createPointLocator();
        DataPointVO dpVO = new DataPointVO();
        dpVO.setPointLocator(plVO);
        
        plVO.setScript("return -1;");
        plVO.setUpdateEvent(UpdateEvent.SECONDS);
                
        MetaDataSourceRT dsRT = (MetaDataSourceRT)dsVO.createDataSourceRT();
        MetaPointLocatorRT plRT = (MetaPointLocatorRT)plVO.createRuntime();
        DataPointRT dpRT = new DataPointRT(dpVO, plRT);
//        plRT.start(dsRT, dpRT);
//        plRT.doPoll(0);
        
        
//        fail("Proto");
    }

}
