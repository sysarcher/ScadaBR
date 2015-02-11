/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao.jdbc;

import br.org.scadabr.ScadaBrVersionBean;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.dao.DataSourceDao;
import br.org.scadabr.dao.PointLinkDao;
import br.org.scadabr.dao.PointValueDao;
import br.org.scadabr.rt.SchedulerPool;
import br.org.scadabr.rt.link.PointLinkManager;
import com.serotonin.mango.db.DatabaseAccessFactory;
import com.serotonin.mango.rt.dataImage.DoubleValueTime;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.DoubleDataPointVO;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author aploese
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PointValuesDaoTest.Config.class})
public class PointValuesDaoTest {

    @Configuration
    public static class Config {
        
        private final PointValueDao pointValueDao = new PointValueDaoImpl();
        
        private final BatchWriteBehind batchWriteBehind =  new BatchWriteBehind();
        
        private final DataPointDao dataPointDao = new DataPointDaoImpl();
        
        private final SchedulerPool schedulerPool = new SchedulerPool();

        private final DatabaseAccessFactory databaseAccessFactory = new DatabaseAccessFactory();
                
        private final ScadaBrVersionBean ScadaBrVersionBean = new ScadaBrVersionBean();
        

        /**
         * No tests with PointLinkManager so return null.
         * @return 
         */
        @Bean
        public PointLinkManager getPointLinkManager() {
            return null;
        }
        
        /**
         * No tests with PointLinkDao so return null.
         * @return 
         */
        @Bean
        public PointLinkDao getPointLinkDao() {
            return null;
        }
        
        @Bean 
        public SchedulerPool getSchedulerPool() {
            return schedulerPool;
        }
        
        @Bean
        public PointValueDao getPointValueDao() {
            return pointValueDao;
        }
        
        @Bean
        public BatchWriteBehind getBatchWriteBehind() {
            return batchWriteBehind;
        }
        
        @Bean
        public DataPointDao getDataPointDao() {
            return dataPointDao;
        }
        
        @Bean
        public DatabaseAccessFactory getDatabaseAccessFactory() {
            return databaseAccessFactory;
        }

        @Bean
        public ScadaBrVersionBean getScadaBrVersionBean() {
            return ScadaBrVersionBean;
        }
        

    }

    public PointValuesDaoTest() {
    }

    @Inject
    private DataPointDao dataPointDao;

    @Inject
    private PointValueDao pointValueDao;

    @Before
    public void setUp() {
    }

    /**
     * Test the whole lifecycle of a statefull event
     */
    @Test
    public void testDoubleValues() {
        
        DataPointVO dpvo = new DoubleDataPointVO();
        dataPointDao.saveDataPoint(dpvo);
        
        long ts1 = System.currentTimeMillis();
        
        DoubleValueTime dvt = new DoubleValueTime(-1.0, dpvo.getId(), ts1);
        
        pointValueDao.savePointValueAsync(dvt, null);
        PointValueTime pvt = pointValueDao.getLatestPointValue(dpvo);
        assertEquals(dvt, pvt);
        
    }

}
