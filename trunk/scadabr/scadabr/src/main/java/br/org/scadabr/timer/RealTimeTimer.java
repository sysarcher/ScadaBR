/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer;

import br.org.scadabr.ImplementMeException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author aploese
 */
public class RealTimeTimer extends AbstractTimer {

    public void execute(Runnable r) {
        throw new ImplementMeException();
    }

    public void init(ThreadPoolExecutor threadPoolExecutor) {
        throw new ImplementMeException();
    }

    public List<TimerTask> cancel() {
        throw new ImplementMeException();
    }

    public ExecutorService getExecutorService() {
        throw new ImplementMeException();
    }

    public int size() {
        throw new ImplementMeException();
    }

}
