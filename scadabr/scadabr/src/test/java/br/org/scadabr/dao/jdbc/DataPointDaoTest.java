/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao.jdbc;

import br.org.scadabr.ScadaBrVersionBean;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.dao.PointLinkDao;
import br.org.scadabr.json.dao.JsonMapperFactory;
import br.org.scadabr.rt.link.PointLinkManager;
import com.serotonin.mango.db.DatabaseAccessFactory;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.DoubleDataPointVO;
import javax.inject.Inject;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author aploese
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataPointDaoTest.Config.class})
public class DataPointDaoTest {

    @Configuration
    public static class Config {
        
        private final DataPointDao dataPointDao = new DataPointDaoImpl();
        
        private final PointLinkDao pointLinkDao = new PointLinkDaoImpl();

        private final DatabaseAccessFactory databaseAccessFactory = new DatabaseAccessFactory();
                
        private final ScadaBrVersionBean ScadaBrVersionBean = new ScadaBrVersionBean();
        
        private final JsonMapperFactory jsonMapperFactory = new JsonMapperFactory();

        /**
         * No tests with PointLinkManager so return null.
         * @return 
         */
        @Bean
        public PointLinkManager getPointLinkManager() {
            return null;
        }
        
        @Bean
        public DataPointDao getDataPointDao() {
            return dataPointDao;
        }
        
        @Bean
        public PointLinkDao getPointLinkDao() {
            return pointLinkDao;
        }
        
        @Bean
        public DatabaseAccessFactory getDatabaseAccessFactory() {
            return databaseAccessFactory;
        }

        @Bean
        public ScadaBrVersionBean getScadaBrVersionBean() {
            return ScadaBrVersionBean;
        }
        
        @Bean
        public JsonMapperFactory getJsonMapperFactory() {
            return jsonMapperFactory;
        }
        

    }

    public DataPointDaoTest() {
    }

    @Inject
    private DataPointDao dataPointDao;

    @Before
    public void setUp() {
    }

    /**
     * Test the whole lifecycle of a statefull event
     */
    @Test
    public void testDoubleDataPointCRUD() {
        
        final DataPointVO dpvo = new DoubleDataPointVO();
        dataPointDao.saveDataPoint(dpvo);
        DataPointVO dpvo1 = dataPointDao.getDataPoint(dpvo.getId());
        dpvo1.setName("TEST1");
        dataPointDao.saveDataPoint(dpvo1);
        DataPointVO dpvo2 = dataPointDao.getDataPoint(dpvo.getId());
        assertEquals(dpvo1, dpvo2);
        assertEquals(dpvo1.getLoggingType(), dpvo2.getLoggingType());
        dataPointDao.deleteDataPoint(dpvo.getId());
        DataPointVO dpvo3 = dataPointDao.getDataPoint(dpvo.getId());
        
        assertNull(dpvo3);
        
    }

}
