package com.serotonin.mango.util.timeout;

import java.util.Date;

import com.serotonin.mango.Common;
import br.org.scadabr.timer.OneTimeTrigger;
import br.org.scadabr.timer.TimerTask;
import br.org.scadabr.timer.TimerTrigger;

@Deprecated//Whats this for?
public class ModelTimeoutTask<T> extends TimerTask {

    private final ModelTimeoutClient<T> client;
    private final T model;

    public ModelTimeoutTask(long delay, ModelTimeoutClient<T> client, T model) {
        this(new OneTimeTrigger(delay), client, model);
    }

    public ModelTimeoutTask(Date date, ModelTimeoutClient<T> client, T model) {
        this(new OneTimeTrigger(date), client, model);
    }

    public ModelTimeoutTask(TimerTrigger trigger, ModelTimeoutClient<T> client, T model) {
        super(trigger);
        this.client = client;
        this.model = model;
        Common.systemCronPool.schedule(this);
    }

    @Override
    protected void run(long runtime) {
        client.scheduleTimeout(model, runtime);
    }
}
