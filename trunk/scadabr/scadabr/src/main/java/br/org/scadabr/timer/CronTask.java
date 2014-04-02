/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.timer;

import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.CronParser;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author aploese
 */
public abstract class CronTask implements Runnable {

    final Object lock = new Object();

    int state = VIRGIN;

    static final int VIRGIN = 0;

    static final int SCHEDULED   = 1;

    static final int EXECUTED    = 2;

    static final int CANCELLED   = 3;
    
    CronExpression cronExpression;

    long nextExecutionTime;
    
    protected long currentTimeInMillis;

    protected CronTask(GregorianCalendar c) {
        cronExpression = new CronExpression(c);
    }

    protected CronTask(CronExpression ce) {
        cronExpression = ce;
    }

    protected CronTask(String pattern) throws ParseException {
        CronParser cp = new CronParser();
        cronExpression = cp.parse(pattern);
    }

    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    public long calcNextExecutionTimeAfter(GregorianCalendar c) {
        synchronized(lock) {
            cronExpression.calcNextValidTimeAfter();
            cronExpression.setNextTimestampTo(c);
            nextExecutionTime = c.getTimeInMillis();
            return nextExecutionTime;
        }
        
    }
    
    public long getNextScheduledExecutionTime() {
        synchronized(lock) {
            return nextExecutionTime;
        }
    }

}
