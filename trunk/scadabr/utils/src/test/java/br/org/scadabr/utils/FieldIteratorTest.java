/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.utils;

import br.org.scadabr.utils.serialization.SerializabeField;
import java.io.Serializable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
public class FieldIteratorTest {
    
    static class A implements Serializable {
        @SerializabeField
        private String aa;
        @SerializabeField
        private String ab;
        private String ac;
    }
    
    static class B extends A{
        @SerializabeField
        private String ba;
        @SerializabeField
        private String bb;
        private String bc;
    }
    
    
    
    public FieldIteratorTest() {
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
     * Test of hasNext method, of class FieldIterator.
     */
    @Test
    public void testAnnotated() {
        B b = new B();
        FieldIterator instance = new FieldIterator(b.getClass(), SerializabeField.class);
        assertTrue(instance.hasNext());
        assertEquals("ba", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("bb", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("aa", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("ab", instance.next().getName());
        assertFalse(instance.hasNext());
    }

    /**
     * Test of next method, of class FieldIterator.
     */
    @Test
    public void testNotAnnotated() {
        B b = new B();
        FieldIterator instance = new FieldIterator(b.getClass());
        assertTrue(instance.hasNext());
        assertEquals("ba", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("bb", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("bc", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("aa", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("ab", instance.next().getName());
        assertTrue(instance.hasNext());
        assertEquals("ac", instance.next().getName());
        assertFalse(instance.hasNext());
    }

}
