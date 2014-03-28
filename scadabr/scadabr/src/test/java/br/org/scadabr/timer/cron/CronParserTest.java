/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.timer.cron;

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
public class CronParserTest {
    
    private CronParser parser;
    
    public CronParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        parser = new CronParser();
    }
    
    @After
    public void tearDown() {
        parser = null;
    }

    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParseANY() {
        System.out.println("parse");
        String cron = "* * * * * * * *";
        CronExpression expResult = createCronExpression();
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParse_Fixed() {
        System.out.println("parse");
        String cron = "0 * * * * * * *";
        CronExpression expResult = createCronExpression(new CronValueField(CronFieldType.MILLIS, 0));
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class CronParser.
     */
    @Test(expected = RuntimeException.class)
    public void testParse_Too_Long() {
        System.out.println("parse");
        String cron = "* * * * * * * * *";
        CronExpression result = parser.parse(cron);
    }


    /**
     * Test of parse method, of class CronParser.
     */
    @Test(expected = RuntimeException.class)
    public void testParse_Too_Short() {
        System.out.println("parse");
        String cron = "* * * * * *";
        CronExpression result = parser.parse(cron);
    }




    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParse_Fixed_TWO() {
        System.out.println("parse");
        String cron = "0,500 * * * * * * *";
        CronExpression expResult = createCronExpression(new CombinedCronField(CronFieldType.MILLIS, new CronValueField(CronFieldType.MILLIS, 0), new CronValueField(CronFieldType.MILLIS, 500)));
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }


    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParse_MS_Range() {
        System.out.println("parse");
        String cron = "100-200 * * * * * * *";
        CronExpression expResult = createCronExpression(new CronRangeField(CronFieldType.MILLIS, 100, 200, 1));
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParse_MS_Range_Increment() {
        System.out.println("parse");
        String cron = "100-600/17 * * * * * * *";
        CronExpression expResult = createCronExpression(new CronRangeField(CronFieldType.MILLIS, 100, 600, 17));
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParse_MS_ANY_Increment() {
        System.out.println("parse");
        String cron = "*/200 * * * * * * *";
        CronExpression expResult = createCronExpression(new CronRangeField(CronFieldType.MILLIS, 0, 999, 200));
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class CronParser.
     */
    @Test
    public void testParse_MS_Range_Increment_Range() {
        System.out.println("parse");
        String cron = "0-499/100,500-999/50 * * * * * * *";
        CronExpression expResult = createCronExpression(new CombinedCronField(CronFieldType.MILLIS, new CronRangeField(CronFieldType.MILLIS, 0, 499, 100), new CronRangeField(CronFieldType.MILLIS, 500, 999, 50)));
        CronExpression result = parser.parse(cron);
        assertEquals(expResult, result);
    }

    private CronExpression createCronExpression(CronField ... cronFields) {
        CronExpression result = new CronExpression();
        result.setField(new AnyField(CronFieldType.MILLIS));
        result.setField(new AnyField(CronFieldType.SEC));
        result.setField(new AnyField(CronFieldType.MIN));
        result.setField(new AnyField(CronFieldType.HOUR));
        result.setField(new AnyField(CronFieldType.DAY_OF_MONTH));
        result.setField(new AnyField(CronFieldType.MONTH_OF_YEAR));
        result.setField(new AnyField(CronFieldType.DAY_OF_WEEK));
        result.setField(new AnyField(CronFieldType.YEAR));    
        if (cronFields != null) {
            for (CronField cf : cronFields) {
                result.setField(cf);
            }
        }
        return result;
    }
}
