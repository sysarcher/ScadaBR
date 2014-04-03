/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.timer;

import br.org.scadabr.ImplementMeException;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.CronParser;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author aploese
 */
public abstract class CronTask {

    protected abstract void run(long executionTime);

    TaskRunner tr;

    TaskRunner createRunner(long scheduledExecutionTime) {
        return new TaskRunner(scheduledExecutionTime);
    }
    
    class TaskRunner implements Runnable {

        final long executionTime;
        
        public TaskRunner(long executionTime) {
            this.executionTime = executionTime;
            synchronized (lock) {
                if (tr == null) {
                    tr = this;
                } else {
                    throw  new ImplementMeException(); // what should happen if the old run is not over and we called again???
                }
            }
        }

        @Override
        public void run() {
            try {
                CronTask.this.run(executionTime);
            } finally {
                synchronized(lock) {
                    if (tr == this) {
                        tr = null;
                    }
                }
            }
        }
    }
    
    final Object lock = new Object();

    int state = VIRGIN;

    static final int VIRGIN = 0;

    static final int SCHEDULED   = 1;

    static final int EXECUTED    = 2;

    static final int CANCELLED   = 3;
    
    CronExpression cronExpression;

    long nextExecutionTime;
    
    protected CronTask(GregorianCalendar c) {
        cronExpression = new CronExpression(c);
    }

    protected CronTask(CronExpression ce) {
        cronExpression = ce;
    }

    protected CronTask(String pattern, TimeZone tz) throws ParseException {
        CronParser cp = new CronParser();
        cronExpression = cp.parse(pattern, tz);
    }

    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    public long calcNextExecutionTimeIncludingNow(long timeInMillis) {
        synchronized(lock) {
            nextExecutionTime = cronExpression.calcNextValidTimeIncludingThis(timeInMillis);
            return nextExecutionTime;
        }
    }
    
    public long calcNextExecutionTimeAfter(GregorianCalendar c) {
        synchronized(lock) {
            nextExecutionTime =  cronExpression.calcNextValidTimeAfter();
            return nextExecutionTime;
        }
    }
    
    public long getNextScheduledExecutionTime() {
        synchronized(lock) {
            return nextExecutionTime;
        }
    }

}
