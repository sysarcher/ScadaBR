/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.rt.SchedulerPool;
import br.org.scadabr.timer.cron.SystemRunnable;
import com.serotonin.mango.db.DatabaseAccess;
import com.serotonin.mango.db.DatabaseAccessFactory;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author aploese
 */
@Named
class BatchWriteBehind {

    class BatchWriteBehindCallable implements Callable<Integer> {

        private JdbcTemplate ejt;
        private Future<Integer> future;
        private boolean sheduledOrRunning;
        private final Object sheduledOrRunningLock = new Object();

        public BatchWriteBehindCallable() {
        }

        @Override
        public Integer call() {
            int entriesWritten = 0;
            try {
                BatchWriteBehindEntry[] inserts;
                while (!entriesToWrite.isEmpty()) {
                    synchronized (entriesToWrite) {
                        inserts = new BatchWriteBehindEntry[entriesToWrite.size() < maxRows ? entriesToWrite.size() : maxRows];
                        for (int i = 0; i < inserts.length; i++) {
                            inserts[i] = entriesToWrite.remove();
                        }
                    }
                    // Create the sql and parameters
                    Object[] params = new Object[inserts.length * POINT_VALUE_INSERT_VALUES_COUNT];
                    StringBuilder sb = new StringBuilder();
                    sb.append(PointValueDao.POINT_VALUE_INSERT_START);
                    for (int i = 0; i < inserts.length; i++) {
                        if (i > 0) {
                            sb.append(',');
                        }
                        sb.append(PointValueDao.POINT_VALUE_INSERT_VALUES);
                        inserts[i].writeInto(params, i);
                    }
                    // Insert the data
                    int retries = 10;
                    while (true) {
                        try {
                            final int count = ejt.update(sb.toString(), params);
                            entriesWritten += count;
                            if (LOG.isLoggable(Level.FINEST)) {
                                LOG.log(Level.FINEST, "Concurrency saving SUCCESS {0}", count);
                            }
                            break;
                        } catch (ConcurrencyFailureException e) {
                            if (retries <= 0) {
                                LOG.log(Level.SEVERE, "Concurrency failure saving {0} batch inserts after 10 tries. Data lost.", inserts.length);
                                break;
                            }
                            int wait = (10 - retries) * 100;
                            try {
                                if (wait > 0) {
                                    synchronized (this) {
                                        wait(wait);
                                    }
                                }
                            } catch (InterruptedException ie) {
                                // no op
                            }
                            retries--;
                        } catch (DataAccessException e) {
                            LOG.log(Level.SEVERE, "Error saving batch inserts. Data lost.", e);
                            break;
                        }
                    }
                }
                return entriesWritten;
            } finally {
                synchronized (sheduledOrRunningLock) {
                    future = null;
                    sheduledOrRunning = false;
                }
            }
        }

    }

    static final int POINT_VALUE_INSERT_VALUES_COUNT = 4;
    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_DAO);
    private final BatchWriteBehindCallable batchWriteBehindCallable;

    @Inject
    private SchedulerPool schedulerPool;
    /**
     *
     * BatchWriteBehindEntry are collected until a minimum size s reached.
     */
    private final Queue<BatchWriteBehindEntry> entriesToWrite = new LinkedList<>();

    private int maxRows;

    public void init(DatabaseAccessFactory daf) {
        final DatabaseAccess databaseAccess = daf.getDatabaseAccess();
        switch (databaseAccess.getType()) {
            case DERBY:
                maxRows = 1000;
                break;
            case MSSQL:
                maxRows = 524;
                break;
            case MYSQL:
                maxRows = 2000;
                break;
            default:
                throw new ShouldNeverHappenException("Unknown database type: " + databaseAccess.getType());
        }
    }

    void add(BatchWriteBehindEntry e, JdbcTemplate ejt) {
        synchronized (entriesToWrite) {
            entriesToWrite.add(e);
            if (entriesToWrite.size() > maxRows) {
                flush(ejt);
            }
        }
    }

    public BatchWriteBehind() {
        batchWriteBehindCallable = new BatchWriteBehindCallable();
    }

    /**
     *
     * @param ejt
     * @return a Future if any data to write otherwise null.
     */
    public Future<Integer> flush(JdbcTemplate ejt) {
        synchronized (batchWriteBehindCallable) {
            Future<Integer> f = batchWriteBehindCallable.future; 
            if (f != null) {
                return f;
            }
            try {
                if (entriesToWrite.isEmpty()) {
                    return null;
                }
                batchWriteBehindCallable.ejt = ejt;
                batchWriteBehindCallable.sheduledOrRunning = true;
                f = schedulerPool.systemPoolInvoke(batchWriteBehindCallable);
                synchronized (batchWriteBehindCallable.sheduledOrRunningLock) {
                    if (batchWriteBehindCallable.sheduledOrRunning) 
                        batchWriteBehindCallable.future = f;
                }
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cant flush", ex);
            }
            return batchWriteBehindCallable.future;
        }
    }

}
