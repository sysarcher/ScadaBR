/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer;

import br.org.scadabr.ImplementMeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author aploese
 */
public class RealTimeTimer extends AbstractTimer {

    ThreadPoolExecutor tpe;
    
    public void execute(Runnable r) {
        tpe.execute(r);
    }

    public void init(ThreadPoolExecutor threadPoolExecutor) {
        this.tpe = threadPoolExecutor;
    }

    @Deprecated
    public List<TimerTask> cancel() {
        return new ArrayList<>(); //TODO
    }

    public int size() {
        throw new ImplementMeException();
    }
    
    public void shutdown() {
        tpe.shutdown();
    }

    @Deprecated
    public ExecutorService getExecutorService() {
        return tpe;
    }
    

}
