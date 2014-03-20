/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.dwr;

import br.org.scadabr.web.i18n.LocalizableMessage;
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
     * Test of addMessage method, of class DwrResponseI18n.
     */
    @Test
    public void testAddMessage_String_ObjectArr() {
        System.out.println("addMessage");
        String i18nKey = "";
        Object[] args = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        instance.addMessage(i18nKey, args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMessage method, of class DwrResponseI18n.
     */
    @Test
    public void testAddMessage_LocalizableMessage() {
        System.out.println("addMessage");
        LocalizableMessage msg = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        instance.addMessage(msg);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addContextualMessage method, of class DwrResponseI18n.
     */
    @Test
    public void testAddContextualMessage() {
        System.out.println("addContextualMessage");
        String i18nKey = "";
        Object[] args = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        instance.addContextualMessage(i18nKey, args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addGenericMessage method, of class DwrResponseI18n.
     */
    @Test
    public void testAddGenericMessage() {
        System.out.println("addGenericMessage");
        String i18nKey = "";
        Object[] args = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        instance.addGenericMessage(i18nKey, args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
     * Test of getHasMessages method, of class DwrResponseI18n.
     */
    @Test
    public void testGetHasMessages() {
        System.out.println("getHasMessages");
        DwrResponseI18n instance = new DwrResponseI18n();
        boolean expResult = false;
        boolean result = instance.getHasMessages();
        assertEquals(expResult, result);
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
     * Test of toString method, of class DwrResponseI18n.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        ResourceBundle b = null;
        DwrResponseI18n instance = new DwrResponseI18n();
        String expResult = "";
        String result = instance.toString(b);
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
