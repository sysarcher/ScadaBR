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

import br.org.scadabr.DataType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.TransactionStatus;

import br.org.scadabr.db.IntValuePair;
import br.org.scadabr.db.RowCallback;
import br.org.scadabr.db.spring.IntValuePairRowMapper;
import br.org.scadabr.io.StreamUtils;
import br.org.scadabr.utils.ImplementMeException;
import com.serotonin.mango.Common;
import com.serotonin.mango.ImageSaveException;
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
import com.serotonin.mango.vo.AnonymousUser;
import com.serotonin.mango.vo.bean.LongPair;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionCallback;

@Named
public class PointValueDao extends BaseDao {

    @Inject
    private Common common;
    @Inject
    private BatchWriteBehind batchWriteBehind;

    private final static List<UnsavedPointValue> UNSAVED_POINT_VALUES = new ArrayList<>();

    static final String POINT_VALUE_INSERT_START = "insert into pointValues (dataPointId, dataType, pointValue, ts) values ";
    static final String POINT_VALUE_INSERT_VALUES = "(?,?,?,?)";
    static final String POINT_VALUE_INSERT = POINT_VALUE_INSERT_START
            + POINT_VALUE_INSERT_VALUES;
    static final String POINT_VALUE_ANNOTATION_INSERT = "insert into pointValueAnnotations "
            + "(pointValueId, textPointValueShort, textPointValueLong, sourceType, sourceId) values (?,?,?,?,?)";

    public PointValueDao() {
        super();
    }

    protected void flushWriteBehind() {
        try {
            final Future<Integer> f = batchWriteBehind.flush(ejt);
            if (f == null) {
                return;
            }
            f.get(); // Just wait to finish
        } catch (InterruptedException | ExecutionException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        // Use our ejt, so that there is no tranaction boundary... // otherwise we wont see the values written there here
        batchWriteBehind.init(daf);
    }

    /**
     * Flush anything
     */
    @PreDestroy
    public void shutdown() {
        flushWriteBehind();
    }

    /**
     * Only the PointValueCache should call this method during runtime. Do not
     * use.
     *
     * @param pointId
     */
    public PointValueTime savePointValueSync(int pointId, PointValueTime pointValue, SetPointSource source) {
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
    public void savePointValueAsync(int pointId, PointValueTime pointValue, SetPointSource source) {
        long id = savePointValueImpl(pointId, pointValue, source, true);
        if (id != -1) {
            clearUnsavedPointValues();
        }
    }

    long savePointValueImpl(final int pointId, final PointValueTime pointValue,
            final SetPointSource source, boolean async) {
        MangoValue value = pointValue.getValue();
        final DataType dataType = value.getDataType();
        double dvalue = 0;
        String svalue = null;

        switch (dataType) {
            case IMAGE:
                ImageValue imageValue = (ImageValue) value;
                dvalue = imageValue.getType();
                if (!imageValue.isNew()) {
                    svalue = Long.toString(imageValue.getId());
                }
                break;
            case BINARY:
            case MULTISTATE:
            case NUMERIC:
                dvalue = value.getDoubleValue();
                break;
            case ALPHANUMERIC:
                svalue = value.getStringValue();
                break;
            default:
                throw new ImplementMeException();
        }

        // Check if we need to create an annotation.
        long id;
        try {
            if (svalue != null || source != null || dataType == DataType.IMAGE) {
                final double dvalueFinal = dvalue;
                final String svalueFinal = svalue;

                // Create a transaction within which to do the insert.
                id = getTransactionTemplate().execute(
                        new TransactionCallback<Long>() {
                            @Override
                            public Long doInTransaction(TransactionStatus status) {
                                return savePointValue(pointId, dataType,
                                        dvalueFinal, pointValue.getTime(),
                                        svalueFinal, source, false);
                            }
                        });
            } else {
                // Single sql call, so no transaction required.
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
        if (dataType == DataType.IMAGE) {
            ImageValue imageValue = (ImageValue) value;
            if (imageValue.isNew()) {
                imageValue.setId(id);

                File file = new File(common.getFiledataPath(),
                        imageValue.getFilename());

                // Write the file.
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    StreamUtils
                            .transfer(
                                    new ByteArrayInputStream(imageValue
                                            .getData()), out);
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

    long savePointValue(final int pointId, final DataType dataType, double dvalue,
            final long time, final String svalue, final SetPointSource source,
            boolean async) {
        // Apply database specific bounds on double values.
        dvalue = daf.getDatabaseAccess().applyBounds(dvalue);

        if (async) {
            batchWriteBehind.add(new BatchWriteBehindEntry(pointId, dataType, dvalue, time), ejt);
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

    private long savePointValueImpl(final int pointId, final DataType dataType, final double dvalue,
            final long time, String svalue, SetPointSource source) {
        long id = doInsertLong(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(POINT_VALUE_INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, pointId);
                ps.setInt(2, dataType.mangoDbId);
                ps.setDouble(3, dvalue);
                ps.setLong(4, time);
                return ps;
            }
        });

        if (svalue == null && dataType == DataType.IMAGE) {
            svalue = Long.toString(id);
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

            ejt.update(POINT_VALUE_ANNOTATION_INSERT, new Object[]{id,
                shortString, longString, sourceType, sourceId}, new int[]{
                Types.INTEGER, Types.VARCHAR, Types.CLOB, Types.SMALLINT,
                Types.INTEGER});
        }

        return id;
    }

    private static final String POINT_VALUE_SELECT = "select pv.dataType, pv.pointValue, pva.textPointValueShort, pva.textPointValueLong, pv.ts, pva.sourceType, "
            + "  pva.sourceId "
            + "from pointValues pv "
            + "  left join pointValueAnnotations pva on pv.id = pva.pointValueId";

    public List<PointValueTime> getPointValues(final int dataPointId, final long since) {
        flushWriteBehind();
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.dataPointId=? and pv.ts >= ? order by ts");
                ps.setInt(1, dataPointId);
                ps.setLong(2, since);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result;
    }

    public List<PointValueTime> getPointValuesBetween(final int dataPointId,
            final long from, final long to) {
        flushWriteBehind();
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts");
                ps.setInt(1, dataPointId);
                ps.setLong(2, from);
                ps.setLong(3, to);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result;
    }

    public List<PointValueTime> getLatestPointValues(final int dataPointId, final int limit) {
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.dataPointId=? order by pv.ts desc");
                ps.setInt(1, dataPointId);
                ps.setMaxRows(limit);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result;
    }

    public List<PointValueTime> getLatestPointValues(final int dataPointId,
            final int limit, final long before) {
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.dataPointId=? and pv.ts<? order by pv.ts desc");
                ps.setInt(1, dataPointId);
                ps.setLong(2, before);
                ps.setMaxRows(limit);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result;
    }

    //TODO replace with queryforObject
    public PointValueTime getLatestPointValue(final int dataPointId) {
        flushWriteBehind();
        //TODO optimaze into one hit of the db???
        final Long maxTs = ejt.queryForObject("select max(ts) from pointValues where dataPointId=?", Long.class, dataPointId);
        if (maxTs == null) {
            return null;
        }
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.dataPointId=? and pv.ts=?");
                ps.setInt(1, dataPointId);
                ps.setLong(2, maxTs);
                ps.setMaxRows(1);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result.get(0);
    }

    private PointValueTime getPointValue(final long id) {
        flushWriteBehind();
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.id=?");
                ps.setLong(1, id);
                ps.setMaxRows(1);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result.get(0);
    }

    public PointValueTime getPointValueBefore(int dataPointId, long time) {
        flushWriteBehind();
        try {
            final Long valueTime = ejt.queryForObject(
                    "select max(ts) from pointValues where dataPointId=? and ts<?",
                    Long.class, dataPointId, time);
            return valueTime == null ? null : getPointValueAt(dataPointId, valueTime);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public PointValueTime getPointValueAt(final int dataPointId, final long time) {
        flushWriteBehind();
        List<PointValueTime> result = ejt.query(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareCall(POINT_VALUE_SELECT
                        + " where pv.dataPointId=? and pv.ts=?");
                ps.setInt(1, dataPointId);
                ps.setLong(2, time);
                ps.setMaxRows(1);
                return ps;

            }
        }, new PointValueRowMapper());
        updateAnnotations(result);
        return result.get(0);
    }

    public void getPointValuesBetween(int dataPointId, long from, long to,
            final RowCallback<PointValueTime> callback) {
        flushWriteBehind();
        ejt.query(POINT_VALUE_SELECT
                + " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts",
                new Object[]{dataPointId, from, to},
                new PointValueRowMapper() {
                    @Override
                    public PointValueTime mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                        final PointValueTime result = super.mapRow(rs, rowNum);
                        callback.row(result, rowNum);
                        return result;
                    }
                });
    }

    class PointValueRowMapper implements RowMapper<PointValueTime> {

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
        final DataType dataType = DataType.fromMangoDbId(rs.getInt(firstParameter));
        MangoValue value;
        switch (dataType) {
            case NUMERIC:
                value = new NumericValue(rs.getDouble(firstParameter + 1));
                break;
            case BINARY:
                value = new BinaryValue(rs.getDouble(firstParameter + 1) == 1);
                break;
            case MULTISTATE:
                value = new MultistateValue(rs.getInt(firstParameter + 1));
                break;
            case ALPHANUMERIC:
                String s = rs.getString(firstParameter + 2);
                if (s == null) {
                    s = rs.getString(firstParameter + 3);
                }
                value = new AlphanumericValue(s);
                break;
            case IMAGE:
                value = new ImageValue(Integer.parseInt(rs
                        .getString(firstParameter + 2)),
                        rs.getInt(firstParameter + 3));
                break;
            default:
                value = null;
        }
        return value;
    }

    private void updateAnnotations(List<PointValueTime> values) {
        Map<Integer, List<AnnotatedPointValueTime>> userIds = new HashMap<>();
        List<AnnotatedPointValueTime> alist;

        // Look for annotated point values.
        AnnotatedPointValueTime apv;
        for (PointValueTime pv : values) {
            if (pv instanceof AnnotatedPointValueTime) {
                apv = (AnnotatedPointValueTime) pv;
                if (apv.getSourceType() == SetPointSource.Types.USER) {
                    alist = userIds.get(apv.getSourceId());
                    if (alist == null) {
                        alist = new ArrayList<>();
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

    private void updateAnnotations(String sql,
            Map<Integer, List<AnnotatedPointValueTime>> idMap) {
        // Get the description information from the database.
        List<IntValuePair> data = ejt.query(
                sql + "(" + createDelimitedList(idMap.keySet(), ",")
                + ")", new IntValuePairRowMapper());

        // Collate the data with the id map, and set the values in the
        // annotations
        List<AnnotatedPointValueTime> annos;
        for (IntValuePair ivp : data) {
            annos = idMap.get(ivp.getKey());
            for (AnnotatedPointValueTime avp : annos) {
                avp.setSourceDescriptionArgument(ivp.getValue());
            }
        }
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
        flushWriteBehind();
        String ids = createDelimitedList(dataPointIds, ",");
        ejt.query(POINT_ID_VALUE_SELECT + " where pv.dataPointId in (" + ids
                + ") and pv.ts >= ? and pv.ts<? order by ts", new Object[]{
                    from, to}, new IdPointValueRowMapper() {
                    @Override
                    public IdPointValueTime mapRow(ResultSet rs, int rowNum) throws SQLException {
                        final IdPointValueTime result = super.mapRow(rs, rowNum);
                        callback.row(result, rowNum);
                        return result;
                    }
                });
    }

    /**
     * Note: this does not extract source information from the annotation.
     */
    class IdPointValueRowMapper implements RowMapper<IdPointValueTime> {

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
                new Object[]{dataPointId});
    }

    public long deleteAllPointData() {
        return deletePointValues("delete from pointValues", null);
    }

    public long deletePointValuesWithMismatchedType(int dataPointId,
            DataType dataType) {
        return deletePointValues(
                "delete from pointValues where dataPointId=? and dataType<>?",
                new Object[]{dataPointId, dataType.mangoDbId});
    }

    private long deletePointValues(String sql, Object[] params) {
        int cnt;
        if (params == null) {
            cnt = ejt.update(sql);
        } else {
            cnt = ejt.update(sql, params);
        }
        clearUnsavedPointValues();
        return cnt;
    }

    public long dateRangeCount(int dataPointId, long from, long to) {
        flushWriteBehind();
        return ejt
                .queryForLong(
                        "select count(*) from pointValues where dataPointId=? and ts>=? and ts<=?",
                        dataPointId, from, to);
    }

    public long getInceptionDate(int dataPointId) {
        flushWriteBehind();
        try {
            return ejt.queryForObject("select min(ts) from pointValues where dataPointId=?",
                    Long.class,
                    dataPointId);
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public long getStartTime(List<Integer> dataPointIds) {
        flushWriteBehind();
        if (dataPointIds.isEmpty()) {
            return -1;
        }
        return ejt
                .queryForLong("select min(ts) from pointValues where dataPointId in ("
                        + createDelimitedList(dataPointIds, ",") + ")");
    }

    public long getEndTime(List<Integer> dataPointIds) {
        flushWriteBehind();
        if (dataPointIds.isEmpty()) {
            return -1;
        }
        return ejt
                .queryForLong("select max(ts) from pointValues where dataPointId in ("
                        + createDelimitedList(dataPointIds, ",") + ")");
    }

    public LongPair getStartAndEndTime(List<Integer> dataPointIds) {
        flushWriteBehind();
        if (dataPointIds.isEmpty()) {
            return null;
        }
        try {
            return ejt.queryForObject(
                    "select min(ts), max(ts) from pointValues where dataPointId in ("
                    + createDelimitedList(dataPointIds, ",") + ")",
                    null, new RowMapper<LongPair>() {
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
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Long> getFiledataIds() {
        flushWriteBehind();
        final StringBuilder sb = new StringBuilder();
        sb.append("select distinct id from ( ");
        sb.append("  select id as id from pointValues where dataType=");
        sb.append(DataType.IMAGE.mangoDbId);
        sb.append("  union");
        sb.append("  select d.pointValueId as id from reportInstanceData d ");
        sb.append("    join reportInstancePoints p on d.reportInstancePointId=p.id");
        sb.append("  where p.dataType=");
        sb.append(DataType.IMAGE.mangoDbId);
        sb.append(") a order by 1");
        return ejt.queryForList(sb.toString(), Long.class);
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

}
