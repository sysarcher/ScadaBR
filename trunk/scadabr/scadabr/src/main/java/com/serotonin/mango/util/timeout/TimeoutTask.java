package com.serotonin.mango.util.timeout;

import java.util.Date;

import com.serotonin.mango.Common;
import br.org.scadabr.timer.OneTimeTrigger;
import br.org.scadabr.timer.TimerTask;
import br.org.scadabr.timer.TimerTrigger;

public class TimeoutTask extends TimerTask {

    private final TimeoutClient client;

    public TimeoutTask(long delay, TimeoutClient client) {
        this(new OneTimeTrigger(delay), client);
    }

    public TimeoutTask(Date date, TimeoutClient client) {
        this(new OneTimeTrigger(date), client);
    }

    public TimeoutTask(TimerTrigger trigger, TimeoutClient client) {
        super(trigger);
        this.client = client;
        Common.timer.schedule(this);
    }

    @Override
    protected void run(long runtime) {
        client.scheduleTimeout(runtime);
    }
}
