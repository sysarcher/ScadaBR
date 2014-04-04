/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.dwr;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author aploese
 */
@Ignore
public class DwrMessageI18nTest {
    
    public DwrMessageI18nTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getI18nKey method, of class DwrMessageI18n.
     */
    @Test
    public void testGetI18nKey() {
        System.out.println("getI18nKey");
        DwrMessageI18n instance = new DwrMessageI18n("contextKey", "contextualMessageKey", "contextualMessageParam1");
        com.serotonin.web.dwr.DwrMessageI18n msg = new com.serotonin.web.dwr.DwrMessageI18n("contextKey", "contextualMessageKey", "contextualMessageParam1");
        String expResult = "";
        String result = instance.getI18nKey();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getArgs method, of class DwrMessageI18n.
     */
    @Test
    public void testGetArgs() {
        System.out.println("getArgs");
        DwrMessageI18n instance = new DwrMessageI18n("contextKey", "contextualMessageKey", "contextualMessageParam1");
        Object[] expResult = null;
        Object[] result = instance.getArgs();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
