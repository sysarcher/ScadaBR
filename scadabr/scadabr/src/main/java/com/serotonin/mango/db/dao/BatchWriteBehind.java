/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.monitor.IntegerMonitor;
import br.org.scadabr.rt.SchedulerPool;
import br.org.scadabr.timer.cron.SystemRunnable;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.DatabaseAccess;
import com.serotonin.mango.db.DatabaseAccessFactory;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
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

    class BatchWriteBehindChunk implements SystemRunnable {

        private final JdbcTemplate ejt;

        public BatchWriteBehindChunk(JdbcTemplate ejt) {
            this.ejt = ejt;
        }
        
        @Override
        public void run() {
            try {
                BatchWriteBehindEntry[] inserts;
                while (true) {
                    synchronized (entriesToWrite) {
                        if (entriesToWrite.size() == 0) {
                            break;
                        }
                        inserts = new BatchWriteBehindEntry[entriesToWrite.size() < maxRows ? entriesToWrite.size() : maxRows];
                        for (int i = 0; i < inserts.length; i++) {
                            inserts[i] = entriesToWrite.remove();
                        }
                        ENTRIES_MONITOR.setValue(entriesToWrite.size());
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
            } finally {
                chunks.remove(this);
                INSTANCES_MONITOR.setValue(chunks.size());
            }
        }

    }

    static final int POINT_VALUE_INSERT_VALUES_COUNT = 4;
    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_DAO);
    private final static int SPAWN_THRESHOLD = 10000;
    private final static int MAX_INSTANCES = 5;

    @Inject
    private SchedulerPool schedulerPool;
    /**
     * 
     * BatchWriteBehindEntry are collected until a minimum size s reached.
     */
    private final Queue<BatchWriteBehindEntry> entriesToWrite = new ArrayDeque<>();

    /**
     * This are the cunks that are written to the database
     */
    private final CopyOnWriteArrayList<BatchWriteBehindChunk> chunks = new CopyOnWriteArrayList<>();
    private int maxRows;
    private final IntegerMonitor ENTRIES_MONITOR = new IntegerMonitor("BatchWriteBehind.ENTRIES_MONITOR", null);
    private final IntegerMonitor INSTANCES_MONITOR = new IntegerMonitor("BatchWriteBehind.INSTANCES_MONITOR", null);

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
            ENTRIES_MONITOR.setValue(entriesToWrite.size());
            if (entriesToWrite.size() > chunks.size() * SPAWN_THRESHOLD) {
                if (chunks.size() < MAX_INSTANCES) {
                    BatchWriteBehindChunk bwb = new BatchWriteBehindChunk(ejt);
                    chunks.add(bwb);
                    INSTANCES_MONITOR.setValue(chunks.size());
                    try {
                        schedulerPool.execute(bwb);
                    } catch (RejectedExecutionException ree) {
                        chunks.remove(bwb);
                        INSTANCES_MONITOR.setValue(chunks.size());
                        throw ree;
                    }
                }
            }
        }
    }

    public BatchWriteBehind() {
        Common.MONITORED_VALUES.addIfMissingStatMonitor(ENTRIES_MONITOR);
        Common.MONITORED_VALUES.addIfMissingStatMonitor(INSTANCES_MONITOR);
    }

}
