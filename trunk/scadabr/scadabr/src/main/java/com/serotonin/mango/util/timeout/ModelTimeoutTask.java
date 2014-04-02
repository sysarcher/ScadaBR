package com.serotonin.mango.util.timeout;

import br.org.scadabr.timer.CronTask;
import br.org.scadabr.timer.cron.CronExpression;
import java.text.ParseException;

public class ModelTimeoutTask<T> extends CronTask {

    private final ModelTimeoutClient<T> client;
    private final T model;

    public ModelTimeoutTask(CronExpression cronExpression, ModelTimeoutClient<T> client, T model) {
        super(cronExpression);
        this.client = client;
        this.model = model;
    }

    public ModelTimeoutTask(String cronPattern, ModelTimeoutClient<T> client, T model) throws ParseException {
        super(cronPattern);
        this.client = client;
        this.model = model;
    }

    @Override
    public void run() {
        client.scheduleTimeout(model, currentTimeInMillis);
    }
}
