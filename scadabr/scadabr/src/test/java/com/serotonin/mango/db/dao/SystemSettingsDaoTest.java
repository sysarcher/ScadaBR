/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import com.serotonin.mango.Common;
import com.serotonin.mango.SysProperties;
import java.awt.Color;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
public class SystemSettingsDaoTest extends AbstractDaoTests {
    
    private SystemSettingsDao systemSettingsDao = new SystemSettingsDao();

    @Before
    public void setUp() {
        systemSettingsDao.setDataSource(applicationContext.getBean(DataSource.class));
    }
    
    private int countSystemSettingsRows() {
        return countRowsInTable("systemSettings");
    }
    
    /**
     * Test of getValue, setValue method, of class SystemSettingsDao.
     */
    @Test
    public void testGetSetValue_SysProperties() {
        System.out.println("testGetSetValue_SysProperties");
        int rows = countSystemSettingsRows();
        assertEquals(SysProperties.EVENT_PURGE_PERIODS.defaultValue, systemSettingsDao.getValue(SysProperties.EVENT_PURGE_PERIODS));
        systemSettingsDao.setValue(SysProperties.EVENT_PURGE_PERIODS, "2");
        assertEquals(1, countSystemSettingsRows() - rows);
        assertEquals("2", systemSettingsDao.getValue(SysProperties.EVENT_PURGE_PERIODS));
    }

    /**
     * Test of getValue setValue method, of class SystemSettingsDao.
     */
    @Test
    public void testGetSetValue_String() {
        System.out.println("setValue");
        int rows = countSystemSettingsRows();
        assertEquals("Default", systemSettingsDao.getValue("StringValue", "Default"));
        systemSettingsDao.setValue("StringValue", "Hello");
        assertEquals(1, countSystemSettingsRows() - rows);
        assertEquals("Hello", systemSettingsDao.getValue("StringValue", "Default"));
    }

    /**
     * Test of getIntValue, getIntValue method, of class SystemSettingsDao.
     */
    @Test
    public void testGetSetIntValue_SysProperties() {
        System.out.println("testGetSetIntValue_SysProperties");
        int rows = countSystemSettingsRows();
        assertEquals(Integer.parseInt(SysProperties.EVENT_PURGE_PERIODS.defaultValue), systemSettingsDao.getIntValue(SysProperties.EVENT_PURGE_PERIODS));
        systemSettingsDao.setIntValue(SysProperties.EVENT_PURGE_PERIODS, 2);
        assertEquals(1, countSystemSettingsRows() - rows);
        assertEquals(2, systemSettingsDao.getIntValue(SysProperties.EVENT_PURGE_PERIODS));
    }

    /**
     * Test of getIntValue, setIntValue method, of class SystemSettingsDao.
     */
    @Test
    public void testGetSetIntValue_String() {
        System.out.println("testGetSetIntValue_String");
        int rows = countSystemSettingsRows();
        assertEquals(11, systemSettingsDao.getIntValue("IntValue1", 11));
        systemSettingsDao.setIntValue("IntValue1", 12);
        assertEquals(1, countSystemSettingsRows() - rows);
        assertEquals(12, systemSettingsDao.getIntValue("IntValue1", 11));
    }

    /**
     * Test of getBooleanValue method, of class SystemSettingsDao.
     */
    @Test
    public void testGetSetBooleanValue_SysProperties() {
        System.out.println("testGetSetBooleanValue_SysProperties");
        int rows = countSystemSettingsRows();
        assertEquals(Boolean.parseBoolean(SysProperties.GROVE_LOGGING.defaultValue), systemSettingsDao.getBooleanValue(SysProperties.GROVE_LOGGING));
        systemSettingsDao.setBooleanValue(SysProperties.GROVE_LOGGING, true);
        assertEquals(1, countSystemSettingsRows() - rows);
        assertEquals(true, systemSettingsDao.getBooleanValue(SysProperties.GROVE_LOGGING));
    }

    /**
     * Test of getBooleanValue method, of class SystemSettingsDao.
     */
    @Test
    public void testSetGetBooleanValue_String() {
        System.out.println("testSetGetBooleanValue");
        int rows = countSystemSettingsRows();
        assertEquals(true, systemSettingsDao.getBooleanValue("BoolValue1", true));
        systemSettingsDao.setBooleanValue("BoolValue1", false);
        assertEquals(1, countSystemSettingsRows() - rows);
        assertEquals(false, systemSettingsDao.getBooleanValue("BoolValue1", true));
    }

    /**
     * Test of removeValue method, of class SystemSettingsDao.
     */
    @Test
    public void testRemoveValue() {
        System.out.println("removeValue");
        final int rows = countSystemSettingsRows();
        systemSettingsDao.removeValue(SysProperties.PRODUCT_VERSION.key);
        assertEquals(1, rows - countSystemSettingsRows());
    }

    /**
     * Test of getFutureDateLimit method, of class SystemSettingsDao.
     */
    @Test
    public void testGetFutureDateLimit() {
        System.out.println("getFutureDateLimit");
        long result = systemSettingsDao.getFutureDateLimit();
        assertEquals(Common.getMillis(
                    Common.TIME_PERIOD_CODES.getId(SysProperties.FUTURE_DATE_LIMIT_PERIOD_TYPE.defaultValue),
                    Integer.parseInt(SysProperties.FUTURE_DATE_LIMIT_PERIODS.defaultValue)), result);
    }

    /**
     * Test of getColour method, of class SystemSettingsDao.
     */
    @Test
    public void testGetColour() {
        System.out.println("getColour");
        Color result = systemSettingsDao.getColor(SysProperties.CHART_BACKGROUND_COLOUR);
        assertEquals(new Color(Long.decode(SysProperties.CHART_BACKGROUND_COLOUR.defaultValue).intValue()), result);
    }

}
