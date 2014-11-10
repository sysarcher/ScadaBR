/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.monitor.IntegerMonitor;
import br.org.scadabr.utils.ImplementMeException;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.DatabaseAccess;
import com.serotonin.mango.rt.maint.work.WorkItem;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author aploese
 */
class BatchWriteBehind implements WorkItem {
    static final int POINT_VALUE_INSERT_VALUES_COUNT = 4;

    private final Queue<BatchWriteBehindEntry> ENTRIES = new ArrayDeque<>();
    private final CopyOnWriteArrayList<BatchWriteBehind> instances = new CopyOnWriteArrayList<>();
    private final Log LOG = LogFactory.getLog(BatchWriteBehind.class);
    private final int SPAWN_THRESHOLD = 10000;
    private final int MAX_INSTANCES = 5;
    private final int MAX_ROWS;
    private final IntegerMonitor ENTRIES_MONITOR = new IntegerMonitor("BatchWriteBehind.ENTRIES_MONITOR", null);
    private final IntegerMonitor INSTANCES_MONITOR = new IntegerMonitor("BatchWriteBehind.INSTANCES_MONITOR", null);

    void add(BatchWriteBehindEntry e, JdbcTemplate ejt) {
        synchronized (ENTRIES) {
            ENTRIES.add(e);
            ENTRIES_MONITOR.setValue(ENTRIES.size());
            if (ENTRIES.size() > instances.size() * SPAWN_THRESHOLD) {
                if (instances.size() < MAX_INSTANCES) {
                    BatchWriteBehind bwb = null; if (true) throw new ImplementMeException(); //TOFO implement this new BatchWriteBehind(ejt);
                    instances.add(bwb);
                    INSTANCES_MONITOR.setValue(instances.size());
                    try {
                        Common.ctx.getBackgroundProcessing().addWorkItem(bwb);
                    } catch (RejectedExecutionException ree) {
                        instances.remove(bwb);
                        INSTANCES_MONITOR.setValue(instances.size());
                        throw ree;
                    }
                }
            }
        }
    }
    private final JdbcTemplate ejt;

    public BatchWriteBehind(JdbcTemplate ejt, DatabaseAccess databaseAccess) {
        this.ejt = ejt;
        switch (databaseAccess.getType()) {
            case DERBY:
                MAX_ROWS = 1000;
                break;
            case MSSQL:
                MAX_ROWS = 524;
                break;
            case MYSQL:
                MAX_ROWS = 2000;
                break;
            default:
                throw new ShouldNeverHappenException("Unknown database type: " + databaseAccess.getType());
        }
        Common.MONITORED_VALUES.addIfMissingStatMonitor(ENTRIES_MONITOR);
        Common.MONITORED_VALUES.addIfMissingStatMonitor(INSTANCES_MONITOR);
    }

    @Override
    public void execute() {
        try {
            BatchWriteBehindEntry[] inserts;
            while (true) {
                synchronized (ENTRIES) {
                    if (ENTRIES.size() == 0) {
                        break;
                    }
                    inserts = new BatchWriteBehindEntry[ENTRIES.size() < MAX_ROWS ? ENTRIES.size() : MAX_ROWS];
                    for (int i = 0; i < inserts.length; i++) {
                        inserts[i] = ENTRIES.remove();
                    }
                    ENTRIES_MONITOR.setValue(ENTRIES.size());
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
                        ejt.update(sb.toString(), params);
                        break;
                    } catch (ConcurrencyFailureException e) {
                        if (retries <= 0) {
                            LOG.error("Concurrency failure saving " + inserts.length + " batch inserts after 10 tries. Data lost.");
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
                        LOG.error("Error saving " + inserts.length + " batch inserts. Data lost.", e);
                        break;
                    }
                }
            }
        } finally {
            instances.remove(this);
            INSTANCES_MONITOR.setValue(instances.size());
        }
    }

    @Override
    public int getPriority() {
        return WorkItem.PRIORITY_HIGH;
    }

}
