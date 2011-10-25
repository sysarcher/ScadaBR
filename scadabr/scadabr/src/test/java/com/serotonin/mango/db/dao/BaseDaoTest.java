/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import java.io.Reader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
public class BaseDaoTest {
    
    private BaseDao baseDao;

    @Before
    public void setUp() {
        baseDao = new BaseDao();
    }

    /**
     * Test of generateUniqueXid method, of class BaseDao.
     */
    @Ignore
    @Test
    public void testGenerateUniqueXid() {
        System.out.println("generateUniqueXid");
        String prefix = "";
        String tableName = "";
        BaseDao instance = new BaseDao();
        String expResult = "";
        String result = instance.generateUniqueXid(prefix, tableName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isXidUnique method, of class BaseDao.
     */
    @Ignore
    @Test
    public void testIsXidUnique() {
        System.out.println("isXidUnique");
        String xid = "";
        int excludeId = 0;
        String tableName = "";
        BaseDao instance = new BaseDao();
        boolean expResult = false;
        boolean result = instance.isXidUnique(xid, excludeId, tableName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of jsonDeserialize method, of class BaseDao.
     */
    @Ignore
    @Test
    public void testJsonDeserialize() throws Exception {
        System.out.println("jsonDeserialize");
        Reader json = null;
        Object o = null;
        BaseDao instance = new BaseDao();
        instance.jsonDeserialize(json, o);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of jsonSerialize method, of class BaseDao.
     */
    @Ignore
    @Test
    public void testJsonSerialize() {
        System.out.println("jsonSerialize");
        Object o = null;
        BaseDao instance = new BaseDao();
        String expResult = "";
        String result = instance.jsonSerialize(o);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
