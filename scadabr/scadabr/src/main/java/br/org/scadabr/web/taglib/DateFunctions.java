/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib;

import br.org.scadabr.ImplementMeException;
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

    public static String getFullSecondTime(long activeTimestamp) {
        throw new ImplementMeException();
    }

    public static String getFullMinuteTime(long reportStartTime) {
        throw new ImplementMeException();
    }

}