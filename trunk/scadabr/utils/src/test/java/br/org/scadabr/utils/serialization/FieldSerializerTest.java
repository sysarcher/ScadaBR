/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.utils.serialization;

import java.io.ByteArrayInputStream;
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
public class FieldSerializerTest {
    
    public enum E {
        A, B, C, D;
    }
    
    public static class A implements Serializable {
        @SerializabeField
        private E oEnum;
        @SerializabeField
        private String oString;
        @SerializabeField
        private Character oChar;
        @SerializabeField
        private char vChar;
        @SerializabeField
        private boolean vBoolean;
        @SerializabeField
        private byte vByte;
        @SerializabeField
        private short vShort;
        @SerializabeField
        private int vInteger;
        @SerializabeField
        private long vLong;
        @SerializabeField
        private float vFloat;
        @SerializabeField
        private double vDouble;
        @SerializabeField
        private Boolean oBoolean;
        @SerializabeField
        private Byte oByte;
        @SerializabeField
        private Short oShort;
        @SerializabeField
        private Integer oInteger;
        @SerializabeField
        private Long oLong;
        @SerializabeField
        private Float oFloat;
        @SerializabeField
        private Double oDouble;
   //currently we do not handle this ...     @SerializabeField  
        private Serializable oSerializable ="I'm serializable";

        public A() {
            
        }
        
        public A(long i) {
            oEnum = E.values()[(int)((i < 0 ? -i : i)% (long)E.values().length)];
            vBoolean = i != 0;
            oBoolean = vBoolean;
            vChar = (char)i;
            oChar = vChar;
            vByte = (byte)i;
            oByte = vByte;
            vShort = (short)i;
            oShort = vShort;
            vInteger = (int)i;
            oInteger = vInteger;
            vLong = i;
            oLong = vLong;
            vFloat = ((float)i) / 3;
            oFloat = vFloat;
            vDouble = ((double)i) / 33;
            oDouble = vDouble;
            oString = oEnum.name()  + (char)0x0A + " " + i + " ";
        }
    }
    
    public FieldSerializerTest() {
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
     * Test of read method, of class FieldSerializer.
     */
    @Test
    public void testRead() throws Exception {
        A a = new A(-1);
        FieldSerializer instance = new FieldSerializer(a);
        byte[] b = new byte[2048];
        int length = instance.read(b);
        char[] c = new char[b.length];
        for (int i = 0; i < b.length; i++ ) {
            c[i] = (char)(b[i] & 0xFF);
        }
        System.err.println("DATA: \"" + String.copyValueOf(c) + "\"");
        assertEquals(473, length);
        
        FieldDeserializer fd =  new FieldDeserializer(A.class.getName());
        A result = (A)fd.deserializeObject(new ByteArrayInputStream(b, 0, length));
        assertEquals(a.vBoolean, result.vBoolean);
        assertEquals(a.oBoolean, result.oBoolean);
        assertEquals(a.vChar, result.vChar);
        assertEquals(a.oChar, result.oChar);
        assertEquals(a.vByte, result.vByte);
        assertEquals(a.oByte, result.oByte);
        assertEquals(a.vShort, result.vShort);
        assertEquals(a.oShort, result.oShort);
        assertEquals(a.vInteger, result.vInteger);
        assertEquals(a.oInteger, result.oInteger);
        assertEquals(a.vLong, result.vLong);
        assertEquals(a.oLong, result.oLong);
        assertEquals(a.vFloat, result.vFloat, Float.MIN_VALUE);
        assertEquals(a.oFloat, result.oFloat);
        assertEquals(a.vDouble, result.vDouble, Double.MIN_VALUE);
        assertEquals(a.oDouble, result.oDouble);
        assertEquals(a.oString, result.oString);
        assertEquals(a.oEnum, result.oEnum);
        
        SerializedFieldList sfl = new SerializedFieldList();
    //    slf.write(b);
        
        
        // objekt nach bin
        // bin nach map 
        // map nach bin
        // bin nach objekt
//                fail();
    }
    
}
