/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.dwr;

import br.org.scadabr.i18n.LocalizableMessage;
import java.util.List;
import java.util.ResourceBundle;
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
public class DwrResponseI18nTest {
    
    public DwrResponseI18nTest() {
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
     * Test of setMessages method, of class DwrResponseI18n.
     */
    @Test
    public void testSetMessages() {
        System.out.println("setMessages");
        List<DwrMessageI18n> l = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        instance.setMessages(l);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMessages method, of class DwrResponseI18n.
     */
    @Test
    public void testGetMessages() {
        System.out.println("getMessages");
        DwrResponseI18n instance = new DwrResponseI18n();
        Iterable<DwrMessageI18n> expResult = null;
        Iterable<DwrMessageI18n> result = instance.getMessages();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addData method, of class DwrResponseI18n.
     */
    @Test
    public void testAddData() {
        System.out.println("addData");
        String name = "";
        Object b = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        instance.addData(name, b);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
