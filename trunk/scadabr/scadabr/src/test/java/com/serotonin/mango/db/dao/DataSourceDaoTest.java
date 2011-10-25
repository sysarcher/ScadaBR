/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

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
    private void setUpDataSources() {
        executeSqlScript("classpath:db/setUp-Table-DataSources.sql", false);
    }

    /**
     * Test of getDataSources method, of class DataSourceDao.
     */
    @Test
    public void testGetDataSources() {
        System.out.println("getDataSources");
        setUpDataSources();
        List dataSources = dataSourceDao.getDataSources();
        assertEquals(countRowsInTable("dataSources"), dataSources.size());
    }

    /**
     * Test of getDataSource method, of class DataSourceDao.
     */
    @Test
    public void testGetDataSource_int() {
        System.out.println("getDataSource");
        setUpDataSources();
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
        setUpDataSources();
        DataSourceVO dataSource = dataSourceDao.getDataSource("DS_000010");
        assertNotNull(dataSource);
        dataSource = dataSourceDao.getDataSource("");
        assertNull(dataSource);
    }

    /**
     * Test of generateUniqueXid method, of class DataSourceDao.
     */
    @Ignore
    @Test
    public void testGenerateUniqueXid() {
        System.out.println("generateUniqueXid");
        DataSourceDao instance = new DataSourceDao();
        String expResult = "";
        String result = instance.generateUniqueXid();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isXidUnique method, of class DataSourceDao.
     */
    @Ignore
    @Test
    public void testIsXidUnique() {
        System.out.println("isXidUnique");
        String xid = "";
        int excludeId = 0;
        DataSourceDao instance = new DataSourceDao();
        boolean expResult = false;
        boolean result = instance.isXidUnique(xid, excludeId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
    @Ignore
    @Test
    public void testDeleteDataSource() {
        System.out.println("deleteDataSource");
        int dataSourceId = 0;
        DataSourceDao instance = new DataSourceDao();
        instance.deleteDataSource(dataSourceId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyPermissions method, of class DataSourceDao.
     */
    @Ignore
    @Test
    public void testCopyPermissions() {
        System.out.println("copyPermissions");
        int fromDataSourceId = 0;
        int toDataSourceId = 0;
        DataSourceDao instance = new DataSourceDao();
        instance.copyPermissions(fromDataSourceId, toDataSourceId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyDataSource method, of class DataSourceDao.
     */
    @Ignore
    @Test
    public void testCopyDataSource() {
        System.out.println("copyDataSource");
        int dataSourceId = 0;
        ResourceBundle bundle = null;
        DataSourceDao instance = new DataSourceDao();
        int expResult = 0;
        int result = instance.copyDataSource(dataSourceId, bundle);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
