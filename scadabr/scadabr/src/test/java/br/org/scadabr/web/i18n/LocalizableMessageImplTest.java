/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.i18n;

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
public class LocalizableMessageImplTest {

    public LocalizableMessageImplTest() {
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

    @Test
    public void testBoolArg() throws Exception {
        System.out.println("testBoolArg");
        LocalizableMessage instance = new LocalizableMessageImpl("i18nKey", true, false, Boolean.TRUE, Boolean.FALSE);
        String expResult = "i18nKey|\\Ztrue|\\Zfalse|\\Ztrue|\\Zfalse|";
        //expResult = new br.org.scadabr.web.i18n.LocalizableMessage(instance.getI18nKey(), instance.getArgs()).serialize();
        String serialized = I18NUtils.serialize(instance);
        assertEquals(expResult, serialized);
        LocalizableMessage deserialized = I18NUtils.deserialize(serialized);
        assertEquals(instance.getI18nKey(), deserialized.getI18nKey());
        assertArrayEquals(instance.getArgs(), deserialized.getArgs());
    }

    @Test
    public void testNumberArg() throws Exception {
        System.out.println("testBoolArg");
        LocalizableMessage instance = new LocalizableMessageImpl("i18nKey", "\\B127", Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE);
        String expResult = "i18nKey|\\\\B127|\\B127|\\S32767|\\I2147483647|\\J9223372036854775807|\\F3.4028235E38|\\D1.7976931348623157E308|";
        //expResult = new br.org.scadabr.web.i18n.LocalizableMessage(instance.getI18nKey(), instance.getArgs()).serialize();
        String serialized = I18NUtils.serialize(instance);
        assertEquals(expResult, serialized);
        LocalizableMessage deserialized = I18NUtils.deserialize(serialized);
        assertEquals(instance.getI18nKey(), deserialized.getI18nKey());
        assertArrayEquals(instance.getArgs(), deserialized.getArgs());
    }

    @Test
    public void testSpecialChars() throws Exception {
        System.out.println("testSpecialChars");
        LocalizableMessage instance = new LocalizableMessageImpl("i18nKey", "\\", "|", "[", "]", "|[dummykey]");
        String expResult = "i18nKey|\\\\|\\||\\[|\\]|\\|\\[dummykey\\]|";
        //expResult = new br.org.scadabr.web.i18n.LocalizableMessage(instance.getI18nKey(), instance.getArgs()).serialize(); // This handles a "\" not correctly, we do ;-)
        String serialized = I18NUtils.serialize(instance);
        assertEquals(expResult, serialized);
        LocalizableMessage deserialized = I18NUtils.deserialize(serialized);
        assertEquals(instance.getI18nKey(), deserialized.getI18nKey());
        assertEquals(expResult, I18NUtils.serialize(deserialized));
    }

    /**
     * Test of getI18nKey method, of class LocalizableMessageImpl.
     */
    @Test
    public void testNested() throws Exception {
        System.out.println("testNested");
        LocalizableMessage instance = new LocalizableMessageImpl("i18nKey", null, new LocalizableMessageImpl("someKey"));
        String expResult = "i18nKey||[someKey|]|";
        //expResult = new br.org.scadabr.web.i18n.LocalizableMessage("i18nKey", null, new br.org.scadabr.web.i18n.LocalizableMessage("someKey")).serialize(); // Orig after a Localizable MSG no | was written
        String serialized = I18NUtils.serialize(instance);
        assertEquals(expResult, serialized);
        LocalizableMessage deserialized = I18NUtils.deserialize(serialized);
        assertEquals(expResult, I18NUtils.serialize(deserialized));
    }
}
