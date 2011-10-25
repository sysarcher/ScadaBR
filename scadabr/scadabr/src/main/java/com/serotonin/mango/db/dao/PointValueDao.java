/*
Mango - Open Source M2M - http://mango.serotoninsoftware.com
Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
@author Matthew Lohbihler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.db.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.serotonin.io.StreamUtils;
import com.serotonin.mango.Common;
import com.serotonin.mango.ImageSaveException;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.mango.rt.dataImage.IdPointValueTime;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.ImageValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.rt.maint.work.WorkItem;
import com.serotonin.mango.vo.AnonymousUser;
import com.serotonin.mango.vo.bean.LongPair;
import com.serotonin.monitor.IntegerMonitor;
import com.serotonin.util.queue.ObjectQueue;
import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointValueDao extends BaseDao {

    private final static List<UnsavedPointValue> UNSAVED_POINT_VALUES = new ArrayList<UnsavedPointValue>();
    private static final String POINT_VALUE_INSERT_START = "insert into pointValues (dataPointId, dataType, pointValue, ts) values ";
    private static final String POINT_VALUE_INSERT_VALUES = "(?,?,?,?)";
    private static final int POINT_VALUE_INSERT_VALUES_COUNT = 4;
    private static final String POINT_VALUE_INSERT = POINT_VALUE_INSERT_START + POINT_VALUE_INSERT_VALUES;
    private static final String POINT_VALUE_ANNOTATION_INSERT = "insert into pointValueAnnotations "
            + "(pointValueId, textPointValueShort, textPointValueLong, sourceType, sourceId) values (?,?,?,?,?)";
    @Autowired
    private Common common;

    /**
     * Only the PointValueCache should call this method during runtime. Do not
     * use.
     */
    public PointValueTime savePointValueSync(int pointId,
            PointValueTime pointValue, SetPointSource source) {
        long id = savePointValueImpl(pointId, pointValue, source, false);

        PointValueTime savedPointValue;
        int retries = 5;
        while (true) {
            try {
                savedPointValue = getPointValue(id);
                break;
            } catch (ConcurrencyFailureException e) {
                if (retries <= 0) {
                    throw e;
                }
                retries--;
            }
        }

        return savedPointValue;
    }

    /**
     * Only the PointValueCache should call this method during runtime. Do not
     * use.
     */
    public void savePointValueAsync(int pointId, PointValueTime pointValue,
            SetPointSource source) {
        long id = savePointValueImpl(pointId, pointValue, source, true);
        if (id != -1) {
            clearUnsavedPointValues();
        }
    }

    long savePointValueImpl(final int pointId, final PointValueTime pointValue,
            final SetPointSource source, boolean async) {
        MangoValue value = pointValue.getValue();
        final MangoDataType dataType = value.getMangoDataType();
        double dvalue = 0;
        String svalue = null;

        if (dataType == MangoDataType.IMAGE) {
            ImageValue imageValue = (ImageValue) value;
            dvalue = imageValue.getType();
            if (imageValue.isSaved()) {
                svalue = Long.toString(imageValue.getId());
            }
        } else if (value.hasDoubleRepresentation()) {
            dvalue = value.getDoubleValue();
        } else {
            svalue = value.getStringValue();
        }

        // Check if we need to create an annotation.
        long id;
        try {
            if (svalue != null || source != null || dataType == MangoDataType.IMAGE) {
                final double dvalueFinal = dvalue;
                final String svalueFinal = svalue;

                // Create a transaction within which to do the insert.
                id = savePointValue(pointId, dataType,
                        dvalueFinal, pointValue.getTime(),
                        svalueFinal, source, false);
            } else // Single sql call, so no transaction required.
            {
                id = savePointValue(pointId, dataType, dvalue,
                        pointValue.getTime(), svalue, source, async);
            }
        } catch (ConcurrencyFailureException e) {
            // Still failed to insert after all of the retries. Store the data
            synchronized (UNSAVED_POINT_VALUES) {
                UNSAVED_POINT_VALUES.add(new UnsavedPointValue(pointId,
                        pointValue, source));
            }
            return -1;
        }

        // Check if we need to save an image
        if (dataType == MangoDataType.IMAGE) {
            ImageValue imageValue = (ImageValue) value;
            if (!imageValue.isSaved()) {
                imageValue.setId(id);

                File file = new File(common.getFiledataPath(),
                        imageValue.getFilename());

                // Write the file.
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    StreamUtils.transfer(
                            new ByteArrayInputStream(imageValue.getData()), out);
                } catch (IOException e) {
                    // Rethrow as an RTE
                    throw new ImageSaveException(e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        // no op
                    }
                }

                // Allow the data to be GC'ed
                imageValue.setData(null);
            }
        }

        return id;
    }

    private void clearUnsavedPointValues() {
        if (!UNSAVED_POINT_VALUES.isEmpty()) {
            synchronized (UNSAVED_POINT_VALUES) {
                while (!UNSAVED_POINT_VALUES.isEmpty()) {
                    UnsavedPointValue data = UNSAVED_POINT_VALUES.remove(0);
                    savePointValueImpl(data.getPointId(), data.getPointValue(),
                            data.getSource(), false);
                }
            }
        }
    }

    public void savePointValue(int pointId, PointValueTime pointValue) {
        savePointValueImpl(pointId, pointValue, new AnonymousUser(), true);
    }

    //TODO is there a need to apply bounds ?? I commented it out ....
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public long savePointValue(final int pointId, final MangoDataType dataType, double dvalue,
            final long time, final String svalue, final SetPointSource source,
            boolean async) {
        // Apply database specific bounds on double values.
//        dvalue = DatabaseAccess.getDatabaseAccess().applyBounds(dvalue);

        if (async) {
            BatchWriteBehind.add(new BatchWriteBehindEntry(pointId, dataType.mangoId,
                    dvalue, time), getSimpleJdbcTemplate(), getMaxRowns());
            return -1;
        }

        int retries = 5;
        while (true) {
            try {
                return savePointValueImpl(pointId, dataType, dvalue, time,
                        svalue, source);
            } catch (ConcurrencyFailureException e) {
                if (retries <= 0) {
                    throw e;
                }
                retries--;
            } catch (RuntimeException e) {
                throw new RuntimeException(
                        "Error saving point value: dataType=" + dataType
                        + ", dvalue=" + dvalue, e);
            }
        }
    }

    private long savePointValueImpl(int pointId, MangoDataType dataType, double dvalue,
            long time, String svalue, SetPointSource source) {

        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("pointValues").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dataPointId", pointId);
        params.put("dataType", dataType.mangoId);
        params.put("pointValue", dvalue);
        params.put("ts", time);

        Number id = insertActor.executeAndReturnKey(params);

        if (svalue == null && dataType == MangoDataType.IMAGE) {
            svalue = Long.toString(id.longValue());
        }

        // Check if we need to create an annotation.
        if (svalue != null || source != null) {
            Integer sourceType = null, sourceId = null;
            if (source != null) {
                sourceType = source.getSetPointSourceType();
                sourceId = source.getSetPointSourceId();
            }

            String shortString = null;
            String longString = null;
            if (svalue != null) {
                if (svalue.length() > 128) {
                    longString = svalue;
                } else {
                    shortString = svalue;
                }
            }

            getSimpleJdbcTemplate().update(POINT_VALUE_ANNOTATION_INSERT, id, shortString, longString, sourceType, sourceId);
        }

        return id.longValue();
    }
    private static final String POINT_VALUE_SELECT = "select pv.dataType, pv.pointValue, pva.textPointValueShort, pva.textPointValueLong, pv.ts, pva.sourceType, "
            + "  pva.sourceId "
            + "from pointValues pv "
            + "  left join pointValueAnnotations pva on pv.id = pva.pointValueId";

    public List<PointValueTime> getPointValues(int dataPointId, long since) {
        return pointValuesQuery_(POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts >= ? order by ts",
                dataPointId, since);
    }

    public List<PointValueTime> getPointValuesBetween(int dataPointId,
            long from, long to) {
        return pointValuesQuery_(
                POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts",
                dataPointId, from, to);
    }

    public List<PointValueTime> getLatestPointValues(int dataPointId, int limit) {
        return pointValuesQueryLimit(POINT_VALUE_SELECT
                + " where pv.dataPointId=? order by pv.ts desc",
                limit,
                dataPointId);
    }

    public List<PointValueTime> getLatestPointValues(int dataPointId,
            int limit, long before) {
        return pointValuesQueryLimit(POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts<? order by pv.ts desc",
                limit,
                dataPointId, before);
    }

    public PointValueTime getLatestPointValue(int dataPointId) {
        Long maxTs = getSimpleJdbcTemplate().queryForObject("select max(ts) from pointValues where dataPointId=?", Long.class, dataPointId);
        if (maxTs == null) {
            return null;
        }
        return pointValueQuery(POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts=?", new Object[]{
                    dataPointId, maxTs});
    }

    private PointValueTime getPointValue(long id) {
        return pointValueQuery(POINT_VALUE_SELECT + " where pv.id=?",
                new Object[]{id});
    }

    public PointValueTime getPointValueBefore(int dataPointId, long time) {
        Long valueTime = getSimpleJdbcTemplate().queryForObject("select max(ts) from pointValues where dataPointId=? and ts<?", Long.class, dataPointId, time);
        if (valueTime == null) {
            return null;
        }
        return getPointValueAt(dataPointId, valueTime);
    }

    public PointValueTime getPointValueAt(int dataPointId, long time) {
        return pointValueQuery(POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts=?", new Object[]{
                    dataPointId, time});
    }

    private PointValueTime pointValueQuery(String sql, Object... params) {
        List<PointValueTime> result = pointValuesQueryLimit(sql, 1, params);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    private List<PointValueTime> pointValuesQuery_(String sql, Object... params) {
        List<PointValueTime> result = getSimpleJdbcTemplate().query(sql, new PointValueRowMapper(), params);
        updateAnnotations(result);
        return result;
    }

    private List<PointValueTime> pointValuesQueryLimit(String sql, int limit, Object... params) {
        List<PointValueTime> result = getLimitJdbcTemplate(limit).query(sql, new PointValueRowMapper(), params);
        updateAnnotations(result);
        return result;
    }

    public void getPointValuesBetween(int dataPointId, long from, long to,
            final RowCallback<PointValueTime> callback) {
        getJdbcTemplate().query(POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts",
                new Object[]{dataPointId, from, to},
                new RowCallbackHandler() {

                    private int rowNum = 0;
                    final PointValueRowMapper rowMapper = new PointValueRowMapper();

                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        callback.row(rowMapper.mapRow(rs, rowNum), rowNum);
                        rowNum += 1;
                    }
                });
    }

    class PointValueRowMapper implements ParameterizedRowMapper<PointValueTime> {

        @Override
        public PointValueTime mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            MangoValue value = createMangoValue(rs, 1);
            long time = rs.getLong(5);

            int sourceType = rs.getInt(6);
            if (rs.wasNull()) // No annotations, just return a point value.
            {
                return new PointValueTime(value, time);
            }

            // There was a source for the point value, so return an annotated
            // version.
            return new AnnotatedPointValueTime(value, time, sourceType,
                    rs.getInt(7));
        }
    }

    MangoValue createMangoValue(ResultSet rs, int firstParameter)
            throws SQLException {
        MangoDataType dataType = MangoDataType.fromMangoId(rs.getInt(firstParameter));
        switch (dataType) {
            case NUMERIC:
                return new NumericValue(rs.getDouble(firstParameter + 1));
            case BINARY:
                return new BinaryValue(rs.getDouble(firstParameter + 1) == 1);
            case MULTISTATE:
                return new MultistateValue(rs.getInt(firstParameter + 1));
            case ALPHANUMERIC:
                String s = rs.getString(firstParameter + 2);
                if (s == null) {
                    s = rs.getString(firstParameter + 3);
                }
                return new AlphanumericValue(s);
            case IMAGE:
                return new ImageValue(Integer.parseInt(rs.getString(firstParameter + 2)),
                        rs.getInt(firstParameter + 3));
            default:
                return null;
        }
    }

    private void updateAnnotations(List<PointValueTime> values) {
        Map<Integer, List<AnnotatedPointValueTime>> userIds = new HashMap<Integer, List<AnnotatedPointValueTime>>();
        List<AnnotatedPointValueTime> alist;

        // Look for annotated point values.
        AnnotatedPointValueTime apv;
        for (PointValueTime pv : values) {
            if (pv instanceof AnnotatedPointValueTime) {
                apv = (AnnotatedPointValueTime) pv;
                if (apv.getSourceType() == SetPointSource.Types.USER) {
                    alist = userIds.get(apv.getSourceId());
                    if (alist == null) {
                        alist = new ArrayList<AnnotatedPointValueTime>();
                        userIds.put(apv.getSourceId(), alist);
                    }
                    alist.add(apv);
                }
            }
        }

        // Get the usernames from the database.
        if (userIds.size() > 0) {
            updateAnnotations("select id, username from users where id in ",
                    userIds);
        }
    }

    private void updateAnnotations(String sql, final Map<Integer, List<AnnotatedPointValueTime>> idMap) {
        // Get the description information from the database.
        final Map<Integer, String> data = new LinkedHashMap();

        getJdbcTemplate().query(
                sql + "(" + createDelimitedList(idMap.keySet(), ",", null)
                + ")", new ResultSetExtractor() {

            @Override
            public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
                // Collate the data with the id map, and set the values in the
                // annotations
                while (rs.next()) {
                    for (AnnotatedPointValueTime avp : idMap.get(rs.getInt(1))) {
                        avp.setSourceDescriptionArgument(rs.getString(2));
                    }

                }
                return null;
            }
        });

    }
    //
    //
    // Multiple-point callback for point history replays
    //
    private static final String POINT_ID_VALUE_SELECT = "select pv.dataPointId, pv.dataType, pv.pointValue, " //
            + "pva.textPointValueShort, pva.textPointValueLong, pv.ts "
            + "from pointValues pv "
            + "  left join pointValueAnnotations pva on pv.id = pva.pointValueId";

    public void getPointValuesBetween(List<Integer> dataPointIds, long from,
            long to, final RowCallback<IdPointValueTime> callback) {
        String ids = createDelimitedList(dataPointIds, ",", null);

        getJdbcTemplate().query(POINT_ID_VALUE_SELECT + " where pv.dataPointId in (" + ids
                + ") and pv.ts >= ? and pv.ts<? order by ts", new Object[]{
                    from, to}, new RowCallbackHandler() {

            private int rowNum = 0;
            final IdPointValueRowMapper rowMapper = new IdPointValueRowMapper();

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.row(rowMapper.mapRow(rs, rowNum), rowNum);
                rowNum += 1;
            }
        });
    }

    /**
     * Note: this does not extract source information from the annotation.
     */
    class IdPointValueRowMapper implements ParameterizedRowMapper<IdPointValueTime> {

        @Override
        public IdPointValueTime mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            int dataPointId = rs.getInt(1);
            MangoValue value = createMangoValue(rs, 2);
            long time = rs.getLong(6);
            return new IdPointValueTime(dataPointId, value, time);
        }
    }

    //
    //
    // Point value deletions
    //
    public long deletePointValuesBefore(int dataPointId, long time) {
        return deletePointValues(
                "delete from pointValues where dataPointId=? and ts<?",
                new Object[]{dataPointId, time});
    }

    public long deletePointValues(int dataPointId) {
        return deletePointValues("delete from pointValues where dataPointId=?",
                dataPointId);
    }

    public long deleteAllPointData() {
        return deletePointValues("delete from pointValues");
    }

    public long deletePointValuesWithMismatchedType(int dataPointId,
            MangoDataType dataType) {
        return deletePointValues(
                "delete from pointValues where dataPointId=? and dataType<>?",
                new Object[]{dataPointId, dataType.mangoId});
    }

    private long deletePointValues(String sql, Object... params) {
        int cnt;
        if (params == null) {
            cnt = getSimpleJdbcTemplate().update(sql);
        } else {
            cnt = getSimpleJdbcTemplate().update(sql, params);
        }
        clearUnsavedPointValues();
        return cnt;
    }

    public long dateRangeCount(int dataPointId, long from, long to) {
        return getSimpleJdbcTemplate().queryForLong("select count(*) from pointValues where dataPointId=? and ts>=? and ts<=?", dataPointId, from, to);
    }

    public long getInceptionDate(int dataPointId) {
        final Long result = getSimpleJdbcTemplate().queryForObject("select min(ts) from pointValues where dataPointId=?", Long.class, dataPointId);
        return result != null ? result : -1;
    }

    public long getStartTime(Collection<Integer> dataPointIds) {
        if (dataPointIds.isEmpty()) {
            return -1;
        }
        return getSimpleJdbcTemplate().queryForLong("select min(ts) from pointValues where dataPointId in ("
                + createDelimitedList(dataPointIds, ",", null) + ")");
    }

    public long getEndTime(Collection<Integer> dataPointIds) {
        if (dataPointIds.isEmpty()) {
            return -1;
        }
        return getSimpleJdbcTemplate().queryForLong("select max(ts) from pointValues where dataPointId in ("
                + createDelimitedList(dataPointIds, ",", null) + ")");
    }

    public LongPair getStartAndEndTime(Collection<Integer> dataPointIds) {
        if (dataPointIds.isEmpty()) {
            return null;
        }
        return getSimpleJdbcTemplate().queryForObject(
                "select min(ts), max(ts) from pointValues where dataPointId in ("
                + createDelimitedList(dataPointIds, ",", null) + ")",
                new ParameterizedRowMapper<LongPair>() {

                    @Override
                    public LongPair mapRow(ResultSet rs, int index)
                            throws SQLException {
                        long l = rs.getLong(1);
                        if (rs.wasNull()) {
                            return null;
                        }
                        return new LongPair(l, rs.getLong(2));
                    }
                });
    }

    public List<Long> getFiledataIds() {
        return getJdbcTemplate().queryForList(
                "select distinct id from ( " //
                + "  select id as id from pointValues where dataType=?"
                + "  union"
                + "  select d.pointValueId as id from reportInstanceData d "
                + "    join reportInstancePoints p on d.reportInstancePointId=p.id"
                + "  where p.dataType=?"
                + ") a order by 1", new Object[]{MangoDataType.IMAGE.mangoId, MangoDataType.IMAGE.mangoId}, Long.class);
    }

    /**
     * Class that stored point value data when it could not be saved to the
     * database due to concurrency errors.
     * 
     * @author Matthew Lohbihler
     */
    class UnsavedPointValue {

        private final int pointId;
        private final PointValueTime pointValue;
        private final SetPointSource source;

        public UnsavedPointValue(int pointId, PointValueTime pointValue,
                SetPointSource source) {
            this.pointId = pointId;
            this.pointValue = pointValue;
            this.source = source;
        }

        public int getPointId() {
            return pointId;
        }

        public PointValueTime getPointValue() {
            return pointValue;
        }

        public SetPointSource getSource() {
            return source;
        }
    }

    class BatchWriteBehindEntry {

        private final int pointId;
        private final int dataType;
        private final double dvalue;
        private final long time;

        public BatchWriteBehindEntry(int pointId, int dataType, double dvalue,
                long time) {
            this.pointId = pointId;
            this.dataType = dataType;
            this.dvalue = dvalue;
            this.time = time;
        }

        public void writeInto(Object[] params, int index) {
            index *= POINT_VALUE_INSERT_VALUES_COUNT;
            params[index++] = pointId;
            params[index++] = dataType;
            params[index++] = dvalue;
            params[index++] = time;
        }
    }

    static class BatchWriteBehind implements WorkItem {

        private final int maxRows;
        private static final ObjectQueue<BatchWriteBehindEntry> ENTRIES = new ObjectQueue<PointValueDao.BatchWriteBehindEntry>();
        private static final CopyOnWriteArrayList<BatchWriteBehind> instances = new CopyOnWriteArrayList<BatchWriteBehind>();
        private final static Logger LOG = LoggerFactory.getLogger(BatchWriteBehind.class);
        private static final int SPAWN_THRESHOLD = 10000;
        private static final int MAX_INSTANCES = 5;
        private static final IntegerMonitor ENTRIES_MONITOR = new IntegerMonitor(
                "BatchWriteBehind.ENTRIES_MONITOR", null);
        private static final IntegerMonitor INSTANCES_MONITOR = new IntegerMonitor(
                "BatchWriteBehind.INSTANCES_MONITOR", null);

        static {
            Common.MONITORED_VALUES.addIfMissingStatMonitor(ENTRIES_MONITOR);
            Common.MONITORED_VALUES.addIfMissingStatMonitor(INSTANCES_MONITOR);
        }

        static void add(BatchWriteBehindEntry e, SimpleJdbcTemplate simpleJdbcEntry, int maxRows) {
            synchronized (ENTRIES) {
                ENTRIES.push(e);
                ENTRIES_MONITOR.setValue(ENTRIES.size());
                if (ENTRIES.size() > instances.size() * SPAWN_THRESHOLD) {
                    if (instances.size() < MAX_INSTANCES) {
                        BatchWriteBehind bwb = new BatchWriteBehind(simpleJdbcEntry, maxRows);
                        instances.add(bwb);
                        INSTANCES_MONITOR.setValue(instances.size());
                        try {
                            Common.ctx.getBackgroundProcessing().addWorkItem(
                                    bwb);
                        } catch (RejectedExecutionException ree) {
                            instances.remove(bwb);
                            INSTANCES_MONITOR.setValue(instances.size());
                            throw ree;
                        }
                    }
                }
            }
        }
        private final SimpleJdbcTemplate simpleJdbcTemplate;

        public BatchWriteBehind(SimpleJdbcTemplate simpleJdbcTemplate, int maxRows) {
            this.maxRows = maxRows;
            this.simpleJdbcTemplate = simpleJdbcTemplate;
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

                        inserts = new BatchWriteBehindEntry[ENTRIES.size() < maxRows ? ENTRIES.size() : maxRows];
                        ENTRIES.pop(inserts);
                        ENTRIES_MONITOR.setValue(ENTRIES.size());
                    }

                    // Create the sql and parameters
                    Object[] params = new Object[inserts.length
                            * POINT_VALUE_INSERT_VALUES_COUNT];
                    StringBuilder sb = new StringBuilder();
                    sb.append(POINT_VALUE_INSERT_START);
                    for (int i = 0; i < inserts.length; i++) {
                        if (i > 0) {
                            sb.append(',');
                        }
                        sb.append(POINT_VALUE_INSERT_VALUES);
                        inserts[i].writeInto(params, i);
                    }

                    // Insert the data
                    int retries = 10;
                    while (true) {
                        try {
                            simpleJdbcTemplate.update(sb.toString(), params);
                            break;
                        } catch (ConcurrencyFailureException e) {
                            if (retries <= 0) {
                                LOG.error("Concurrency failure saving "
                                        + inserts.length
                                        + " batch inserts after 10 tries. Data lost.");
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
                        } catch (RuntimeException e) {
                            LOG.error("Error saving " + inserts.length
                                    + " batch inserts. Data lost.", e);
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
}
