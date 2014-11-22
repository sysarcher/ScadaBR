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

import br.org.scadabr.ScadaBrConstants;
import br.org.scadabr.l10n.AbstractLocalizer;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.util.StringUtils;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.vo.event.type.AuditEventSource;
import java.sql.Connection;
import java.sql.Statement;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionCallback;

@Named
public class DataSourceDao extends BaseDao {

    private static final String DATA_SOURCE_SELECT = "select id, xid, name, data from dataSources ";

    @Inject
    private DataPointDao dataPointDao;
    @Inject
    private MaintenanceEventDao maintenanceEventDao;

    public DataSourceDao() {
        super();
    }

    public List<DataSourceVO<?>> getDataSources() {
        List<DataSourceVO<?>> dss = ejt.query(DATA_SOURCE_SELECT + " order by name asc", new DataSourceRowMapper());
        return dss;
    }

    public DataSourceVO<?> getDataSource(int id) {
        try {
            return ejt.queryForObject(DATA_SOURCE_SELECT + " where id=?", new DataSourceRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public DataSourceVO<?> getDataSource(String xid) {
        try {
            return ejt.queryForObject(DATA_SOURCE_SELECT + " where xid=?", new DataSourceRowMapper(), xid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    class DataSourceRowMapper implements RowMapper<DataSourceVO<?>> {

        @Override
        public DataSourceVO<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
            DataSourceVO<?> ds = (DataSourceVO<?>) SerializationHelper.readObject(rs.getBlob(4).getBinaryStream());
            ds.setId(rs.getInt(1));
            ds.setXid(rs.getString(2));
            ds.setName(rs.getString(3));
            return ds;
        }
    }

    public String generateUniqueXid() {
        return generateUniqueXid(DataSourceVO.XID_PREFIX, "dataSources");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "dataSources");
    }

    public void saveDataSource(final DataSourceVO<?> vo) {
        // Decide whether to insert or update.
        if (vo.isNew()) {
            insertDataSource(vo);
        } else {
            updateDataSource(vo);
        }
    }

    private void insertDataSource(final DataSourceVO<?> vo) {
        if (vo.getXid() == null || vo.getXid().isEmpty()) {
            vo.setXid(generateUniqueXid());
        }
        final int id = doInsert(new PreparedStatementCreator() {

            final static String SQL_INSERT = "insert into dataSources (xid, name, dataSourceType, data) values (?,?,?,?)";

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, vo.getXid());
                ps.setString(2, vo.getName());
                ps.setInt(3, vo.getType().getId());
                ps.setBlob(4, SerializationHelper.writeObject(vo));
                return ps;
            }
        });
        vo.setId(id);
        AuditEventType.raiseAddedEvent(AuditEventSource.DATA_SOURCE, vo);
    }

    @SuppressWarnings("unchecked")
    private void updateDataSource(final DataSourceVO<?> vo) {
        DataSourceVO<?> old = getDataSource(vo.getId());

        ejt.update(new PreparedStatementCreator() {

            final static String SQL_UPDATE = "update dataSources set xid=?, name=?, data=? where id=?";

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(SQL_UPDATE);
                ps.setString(1, vo.getXid());
                ps.setString(2, vo.getName());
                ps.setBlob(3, SerializationHelper.writeObject(vo));
                ps.setInt(4, vo.getId());
                return ps;
            }
        });
        //if datasource's name has changed, update datapoints 
        if (!vo.getName().equals(old.getName())) {
            List<DataPointVO> dpList = dataPointDao.getDataPoints(vo.getId(), null);
            for (DataPointVO dp : dpList) {
                dp.setDataSourceName(vo.getName());
                dp.setDeviceName(vo.getName());
                dataPointDao.updateDataPoint(dp);
            }

        }

        AuditEventType.raiseChangedEvent(AuditEventSource.DATA_SOURCE, old, (ChangeComparable<DataSourceVO<?>>) vo);
    }

    public void deleteDataSource(final int dataSourceId) {
        DataSourceVO<?> vo = getDataSource(dataSourceId);
        final JdbcTemplate ejt2 = ejt;

        dataPointDao.deleteDataPoints(dataSourceId);

        if (vo != null) {
            getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    maintenanceEventDao.deleteMaintenanceEventsForDataSource(dataSourceId);
                    ejt2.update("delete from eventHandlers where eventTypeId=" + EventSources.DATA_SOURCE.mangoDbId
                            + " and eventTypeRef1=?", new Object[]{dataSourceId});
                    ejt2.update("delete from dataSourceUsers where dataSourceId=?", new Object[]{dataSourceId});
                    ejt2.update("delete from dataSources where id=?", new Object[]{dataSourceId});
                }
            });

            AuditEventType.raiseDeletedEvent(AuditEventSource.DATA_SOURCE, vo);
        }
    }

    public void copyPermissions(final int fromDataSourceId, final int toDataSourceId) {
        final List<Integer> userIds = ejt.queryForList("select userId from dataSourceUsers where dataSourceId=?", Integer.class, fromDataSourceId);

        ejt.batchUpdate("insert into dataSourceUsers values (?,?)", new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return userIds.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, toDataSourceId);
                ps.setInt(2, userIds.get(i));
            }
        });
    }

    public int copyDataSource(final int dataSourceId, final ResourceBundle bundle) {
        return getTransactionTemplate().execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus status) {

                DataSourceVO<?> dataSource = getDataSource(dataSourceId);

                // Copy the data source.
                DataSourceVO<?> dataSourceCopy = dataSource.copy();
                dataSourceCopy.setId(ScadaBrConstants.NEW_ID);
                dataSourceCopy.setXid(generateUniqueXid());
                dataSourceCopy.setEnabled(false);
                dataSourceCopy.setName(StringUtils.truncate(
                        AbstractLocalizer.localizeI18nKey("common.copyPrefix", bundle, dataSource.getName()), 40));
                saveDataSource(dataSourceCopy);

                // Copy permissions.
                copyPermissions(dataSource.getId(), dataSourceCopy.getId());

                // Copy the points.
                for (DataPointVO dataPoint : dataPointDao.getDataPoints(dataSourceId, null)) {
                    DataPointVO dataPointCopy = dataPoint.copy();
                    dataPointCopy.setId(ScadaBrConstants.NEW_ID);
                    dataPointCopy.setXid(dataPointDao.generateUniqueXid());
                    dataPointCopy.setName(dataPoint.getName());
                    dataPointCopy.setDataSourceId(dataSourceCopy.getId());
                    dataPointCopy.setDataSourceName(dataSourceCopy.getName());
                    dataPointCopy.setDeviceName(dataSourceCopy.getName());
                    dataPointCopy.setEnabled(dataPoint.isEnabled());
                    dataPointCopy.getComments().clear();

                    // Copy the event detectors
                    for (PointEventDetectorVO ped : dataPointCopy.getEventDetectors()) {
                        ped.setId(ScadaBrConstants.NEW_ID);
                        ped.njbSetDataPoint(dataPointCopy);
                    }

                    dataPointDao.saveDataPoint(dataPointCopy);

                    // Copy permissions
                    dataPointDao.copyPermissions(dataPoint.getId(), dataPointCopy.getId());
                }

                return dataSourceCopy.getId();
            }
        });
    }

    public Object getPersistentData(int id) {
        return ejt.query("select rtdata from dataSources where id=?", new Object[]{id},
                new ResultSetExtractor<Serializable>() {
                    @Override
                    public Serializable extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (!rs.next()) {
                            return null;
                        }

                        Blob blob = rs.getBlob(1);
                        if (blob == null) {
                            return null;
                        }

                        return (Serializable) SerializationHelper.readObject(blob.getBinaryStream());
                    }
                });
    }

    public void savePersistentData(final int id, final Object data) {
        ejt.update(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                final PreparedStatement ps = con.prepareStatement("update dataSources set rtdata=? where id=?");
                ps.setBlob(1, SerializationHelper.writeObject(data));
                ps.setInt(2, id);
                return ps;
            }
        });
    }
}
