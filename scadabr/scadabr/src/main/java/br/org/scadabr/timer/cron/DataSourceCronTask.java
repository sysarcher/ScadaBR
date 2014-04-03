/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer.cron;

import br.org.scadabr.timer.CronTask;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import java.text.ParseException;
import java.util.TimeZone;

/**
 *
 * @author aploese
 */
public class DataSourceCronTask extends CronTask {

    private final PollingDataSource dataSource;
    
    public DataSourceCronTask(PollingDataSource dataSource, String cronPattern, TimeZone tz) throws ParseException {
        super(cronPattern, tz);
        this.dataSource = dataSource;
    }

    @Override
    protected void run(long scheduledExecutionTime) {
        dataSource.collectData(scheduledExecutionTime);
    }

}
