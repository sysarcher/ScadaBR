package com.serotonin.mango.rt.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.util.ILifecycle;

abstract public class SendThread extends Thread implements ILifecycle {
    private final static Logger LOG = LoggerFactory.getLogger(SendThread.class);
    private boolean running;

    public SendThread(String threadName) {
        super(threadName);
    }

    public void initialize() {
        running = true;
        start();
    }

    public void terminate() {
        running = false;
        synchronized (this) {
            notify();
        }
    }

    protected boolean isRunning() {
        return running;
    }

    public void joinTermination() {
        try {
            this.join();
        }
        catch (InterruptedException e) {
            // no op
        }
    }

    @Override
    public void run() {
        try {
            runImpl();
        }
        catch (Exception e) {
            LOG.error("Send thread " + getName() + " failed with an exception", e);
        }
    }

    protected void waitImpl(long time) {
        synchronized (this) {
            try {
                wait(time);
            }
            catch (InterruptedException e1) {
                // no op
            }
        }
    }

    abstract protected void runImpl();
}
