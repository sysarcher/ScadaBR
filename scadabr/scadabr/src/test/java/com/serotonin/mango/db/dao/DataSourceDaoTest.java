/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO;
import java.util.List;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
@ContextConfiguration
public class DataSourceDaoTest  extends AbstractDaoTests{

    private DataSourceDao dataSourceDao = new DataSourceDao();
    
    @Before
    public void setUp() {
        dataSourceDao.setDataSource(super.applicationContext.getBean(DataSource.class));
    }
    
    @Transactional(readOnly=false, propagation= Propagation.REQUIRES_NEW)
    private void setUpTableDataSources() {
        executeSqlScript("classpath:db/setUp-Table-DataSources.sql", false);
    }

    private int countDataSourcesRows() {
        return countRowsInTable("dataSources");
    }
    
    /**
     * Test of getDataSources method, of class DataSourceDao.
     */
    @Test
    public void testGetDataSources() {
        System.out.println("getDataSources");
        setUpTableDataSources();
        List dataSources = dataSourceDao.getDataSources();
        assertEquals(countRowsInTable("dataSources"), dataSources.size());
    }

    /**
     * Test of getDataSource method, of class DataSourceDao.
     */
    @Test
    public void testGetDataSource_int() {
        System.out.println("getDataSource");
        setUpTableDataSources();
        DataSourceVO dataSource = dataSourceDao.getDataSource(-11);
        assertNotNull(dataSource);
        dataSource = dataSourceDao.getDataSource(0);
        assertNull(dataSource);
    }

    /**
     * Test of getDataSource method, of class DataSourceDao.
     */
    @Test
    public void testGetDataSource_String() {
        System.out.println("getDataSource");
        setUpTableDataSources();
        DataSourceVO dataSource = dataSourceDao.getDataSource("DS_000010");
        assertNotNull(dataSource);
        dataSource = dataSourceDao.getDataSource("");
        assertNull(dataSource);
    }

    /**
     * Test of generateUniqueXid method, of class DataSourceDao.
     */
    @Test
    public void testGenerateUniqueXid() {
        System.out.println("generateUniqueXid");
        assertNotNull(dataSourceDao.generateUniqueXid());
    }

    /**
     * Test of isXidUnique method, of class DataSourceDao.
     */
    @Test
    public void testIsXidUnique() {
        System.out.println("isXidUnique");
        setUpTableDataSources();
        assertFalse(dataSourceDao.isXidUnique("DS_000012", Common.NEW_ID));
        assertTrue(dataSourceDao.isXidUnique("DS_XXXXXX", Common.NEW_ID));
    }

    /**
     * Test of saveDataSource method, of class DataSourceDao.
     */
    @Test
    public void testSaveDataSource() {
        System.out.println("saveDataSource");

        VMStatDataSourceVO dataSource = new VMStatDataSourceVO();
        
        IMocksControl ctl = EasyMock.createStrictControl();
        EventManager eventManager = ctl.createMock("eventmanager", EventManager.class);
        dataSourceDao.setEventManager(eventManager);
        eventManager.raiseAddedEvent(AuditEventType.TYPE_DATA_SOURCE, dataSource);
        ctl.replay();
        
        dataSourceDao.saveDataSource(dataSource);

        ctl.verify();
        DataSourceVO<?> dataSourceById = dataSourceDao.getDataSource(dataSource.getId());
        assertEquals(dataSource, dataSourceById);

        ctl.reset();
        eventManager.raiseChangedEvent(AuditEventType.TYPE_DATA_SOURCE, dataSource, (ChangeComparable<DataSourceVO<?>>) dataSourceById);
        ctl.replay();
        
        dataSource.setName("testSaveDataSource");
        dataSourceDao.saveDataSource(dataSource);
        ctl.verify();
        
        dataSourceById = dataSourceDao.getDataSource(dataSource.getId());
        assertEquals(dataSource.getName(), dataSourceById.getName());
    }

    /**
     * Test of deleteDataSource method, of class DataSourceDao.
     */
    @Test
    public void testDeleteDataSource() {
        System.out.println("deleteDataSource");
        setUpTableDataSources();
        final DataSourceVO<?> dataSourceVo = dataSourceDao.getDataSource(-11);
        final int rows = countDataSourcesRows();
        
        IMocksControl ctl = EasyMock.createStrictControl();
        DataPointDao dataPointDao = ctl.createMock("dataPointDao", DataPointDao.class);
        MaintenanceEventDao maintenanceEventDao = ctl.createMock("maintenanceEventDao", MaintenanceEventDao.class);
        EventManager eventManager = ctl.createMock("eventManager", EventManager.class); 
        
        dataPointDao.deleteDataPoints(dataSourceVo);
        maintenanceEventDao.deleteMaintenanceEventsForDataSource(dataSourceVo);
        eventManager.raiseDeletedEvent(AuditEventType.TYPE_DATA_SOURCE, dataSourceVo);

        ctl.replay();
        dataSourceDao.setDataPointDao(dataPointDao);
        dataSourceDao.setMaintenanceEventDao(maintenanceEventDao);
        dataSourceDao.setEventManager(eventManager);
        
        dataSourceDao.deleteDataSource(dataSourceVo);
        ctl.verify();

        assertEquals(-1, countDataSourcesRows() - rows);
  }

    /**
     * Test of copyDataSource method, of class DataSourceDao.
     */
    //TODO multi table test
    @Ignore
    @Test
    public void testCopyDataSource() {
        System.out.println("copyDataSource");
        DataSourceVO<?> dataSource = null;
        ResourceBundle bundle = null;
        DataSourceDao instance = new DataSourceDao();
        int expResult = 0;
        int result = instance.copyDataSource(dataSource, bundle);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
