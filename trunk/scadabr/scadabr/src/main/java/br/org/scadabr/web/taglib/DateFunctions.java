/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib;

import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author aploese
 */
public class DateFunctions {

    
    public static String getTime(long ts) {
        //TODO getLocale!!!
        return DateFormat.getTimeInstance().format(new Date(ts));
    }

    public static String getFullSecondTime(long ts) {
        //TODO getLocale!!!
        return DateFormat.getDateTimeInstance().format(new Date(ts));
    }

    public static String getFullMinuteTime(long ts) {
        //TODO getLocale!!!
        return DateFormat.getDateTimeInstance().format(new Date(ts));
    }

}