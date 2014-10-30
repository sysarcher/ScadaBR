/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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
public class SerializationHelperTest {

    public SerializationHelperTest() {
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
     * Test of readObject method, of class SerializationHelper.
     */
    @Test
    @Ignore
    public void testReadObject() {
        System.out.println("readObject");
        InputStream is = null;
        Object expResult = null;
        Object result = SerializationHelper.readObject(is);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeObject method, of class SerializationHelper.
     */
    @Test
    @Ignore
    public void testWriteObject() {
        System.out.println("writeObject");
        Object data = null;
        InputStream expResult = null;
        InputStream result = SerializationHelper.writeObject(data);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeSafeUTF method, of class SerializationHelper.
     */
    @Test
    @Ignore
    public void testWriteSafeUTF() throws Exception {
        System.out.println("writeSafeUTF");
        ObjectOutputStream out = null;
        String str = "";
        SerializationHelper.writeSafeUTF(out, str);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of readSafeUTF method, of class SerializationHelper.
     */
    @Test
    @Ignore
    public void testReadSafeUTF() throws Exception {
        System.out.println("readSafeUTF");
        ObjectInputStream in = null;
        String expResult = "";
        String result = SerializationHelper.readSafeUTF(in);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeObjectToArray method, of class SerializationHelper.
     */
    @Test
    @Ignore
    public void testWriteObjectToArray() {
        System.out.println("writeObjectToArray");
        Object o = null;
        byte[] expResult = null;
        byte[] result = SerializationHelper.writeObjectToArray(o);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    class ClassChangeIs extends ObjectInputStream {

        public ClassChangeIs(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            final ObjectStreamClass osc = super.readClassDescriptor();
            switch (osc.getName()) {
                case "br.org.scadabr.util.DummyA":
                    return ObjectStreamClass.lookup(DummyB.class);
                default:
                    return osc;
            }
        }
    }

    public Object readObject(InputStream is) {
        try (ObjectInputStream ois = new ClassChangeIs(is)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test of writeObjectToArray method, of class SerializationHelper.
     */
    @Test
    public void testChangeClass() {
        System.out.println("change class");
        DummyA a = new DummyA();
        byte[] result = SerializationHelper.writeObjectToArray(a);
        DummyB b = (DummyB) readObject(new ByteArrayInputStream(result));
    }
}
