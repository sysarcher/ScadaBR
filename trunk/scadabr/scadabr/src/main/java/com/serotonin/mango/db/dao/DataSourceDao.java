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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.json.JsonException;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.DataSourceRegistry;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * THis class handles the storage and retieving of datasources
 *
 * @author aploese
 */
@Service
public class DataSourceDao extends BaseDao {

    /** dataPointDao bean to handle storage  and retrieving of the datatpoints of the dataSource */
    private DataPointDao dataPointDao;
    private MaintenanceEventDao maintenanceEventDao;
    private EventManager eventManager;

    @Transactional(readOnly = true)
    public List<DataSourceVO<?>> getDataSources() {
        List<DataSourceVO<?>> dss = getJdbcTemplate().query(DataSourceRowMapper.DATA_SOURCE_SELECT, new DataSourceRowMapper());
        Collections.sort(dss, new DataSourceNameComparator());
        return dss;
    }

    /**
     * @return the dataPointDao
     */
    public DataPointDao getDataPointDao() {
        return dataPointDao;
    }

    /**
     * @param dataPointDao the dataPointDao to set
     */
    @Autowired()
    public void setDataPointDao(DataPointDao dataPointDao) {
        this.dataPointDao = dataPointDao;
    }

    /**
     * @return the maintenanceEventDao
     */
    public MaintenanceEventDao getMaintenanceEventDao() {
        return maintenanceEventDao;
    }

    /**
     * @param maintenanceEventDao the maintenanceEventDao to set
     */
    @Autowired()
    public void setMaintenanceEventDao(MaintenanceEventDao maintenanceEventDao) {
        this.maintenanceEventDao = maintenanceEventDao;
    }

    /**
     * @return the eventManager
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * @param eventManager the eventManager to set
     */
    @Autowired()
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    static class DataSourceNameComparator implements Comparator<DataSourceVO<?>> {

        @Override
        public int compare(DataSourceVO<?> ds1, DataSourceVO<?> ds2) {
            if (StringUtils.isEmpty(ds1.getName())) {
                return -1;
            }
            return ds1.getName().compareToIgnoreCase(ds2.getName());
        }
    }

    @Transactional(readOnly = true)
    public DataSourceVO<?> getDataSource(int id) {
        try {
            return getJdbcTemplate().queryForObject(DataSourceRowMapper.DATA_SOURCE_SELECT + " where id=?", new DataSourceRowMapper(), id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public DataSourceVO<?> getDataSource(String xid) {
        try {
            return getJdbcTemplate().queryForObject(DataSourceRowMapper.DATA_SOURCE_SELECT + " where xid=?", new DataSourceRowMapper(),
                    xid);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    class DataSourceRowMapper implements ParameterizedRowMapper<DataSourceVO<?>> {

        private static final String DATA_SOURCE_SELECT = "select id, xid, dataSourceType, dataSourceName, jsonSerialized from dataSources ";

        @Override
        public DataSourceVO<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final DataSourceRegistry dataSourceRegistry = DataSourceRegistry.valueOf(rs.getString("dataSourceType"));
            final DataSourceVO<?> result = dataSourceRegistry.createDataSourceVO();
            try {
                jsonDeserialize(rs.getClob("jsonSerialized").getCharacterStream(), result);
            } catch (JsonException ex) {
                //TODO what should we do here???
//                eventManager.raiseEvent(null, rowNum, true, null);
            }
            result.setId(rs.getInt("id"));
            result.setXid(rs.getString("xid"));
            result.setName(rs.getString("dataSourceName"));
            return result;
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
        if (vo.getId() == Common.NEW_ID) {
            vo.setXid(generateUniqueXid());
            insertDataSource(vo);
        } else {
            updateDataSource(vo);
        }
    }

    /**
     * @param vo
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void insertDataSource(final DataSourceVO<?> vo) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("dataSources").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap();
        params.put("xid", vo.getXid());
        params.put("dataSourceName", vo.getName());
        params.put("dataSourceType", vo.getType().name());
        params.put("jsonSerialized", jsonSerialize(vo));

        Number id = insertActor.executeAndReturnKey(params);
        vo.setId(id.intValue());

        eventManager.raiseAddedEvent(AuditEventType.TYPE_DATA_SOURCE, vo);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void updateDataSource(final DataSourceVO<?> vo) {

        final DataSourceVO<?> old = getDataSource(vo.getId());

        final String UPDATE_SQL = "update dataSources "
                + "set "
                + " xid=:xid, "
                + " dataSourceName=:dataSourceName, "
                + " jsonSerialized=:jsonSerialized "
                + "where id=:id";
        Map<String, Object> params = new HashMap();
        params.put("xid", vo.getXid());
        params.put("dataSourceName", vo.getName());
        params.put("jsonSerialized", jsonSerialize(vo));
        params.put("id", vo.getId());

        getSimpleJdbcTemplate().update(UPDATE_SQL, params);

        eventManager.raiseChangedEvent(AuditEventType.TYPE_DATA_SOURCE, old, (ChangeComparable<DataSourceVO<?>>) vo);
    }

    //TODO split eventHandlers in multiple tables with "humnan readable" col names ...
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteDataSource(final DataSourceVO<?> dataSourceVo) {

        dataPointDao.deleteDataPoints(dataSourceVo);

        maintenanceEventDao.deleteMaintenanceEventsForDataSource(dataSourceVo);
        //TODO via cascade in db???
        getSimpleJdbcTemplate().update("delete from eventHandlers where eventTypeId=" + EventType.EventSources.DATA_SOURCE
                + " and eventTypeRef1=?", dataSourceVo.getId());
        getSimpleJdbcTemplate().update("delete from dataSources where id=?", dataSourceVo.getId());

        eventManager.raiseDeletedEvent(AuditEventType.TYPE_DATA_SOURCE, dataSourceVo);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    private void copyPermissions(final DataSourceVO<?> fromDataSource, final DataSourceVO<?> toDataSource) {
        final List<Integer> userIds = getJdbcTemplate().queryForList("select userId from dataSourceUsers where dataSourceId=?",
                new Object[]{fromDataSource.getId()}, Integer.class);

        getJdbcTemplate().batchUpdate("insert into dataSourceUsers values (?,?)", new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return userIds.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, toDataSource.getId());
                ps.setInt(2, userIds.get(i));
            }
        });
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public int copyDataSource(final DataSourceVO<?> dataSourceVo, final ResourceBundle bundle) {

        DataSourceVO<?> dataSourceCopy = (DataSourceVO<?>) dataSourceVo.clone();
        dataSourceCopy.setId(Common.NEW_ID);
        dataSourceCopy.setXid(generateUniqueXid());
        dataSourceCopy.setEnabled(false);
        dataSourceCopy.setName(StringUtils.truncate(
                LocalizableMessage.getMessage(bundle, "common.copyPrefix", dataSourceVo.getName()), 40));
        saveDataSource(dataSourceCopy);

        // Copy permissions.
        copyPermissions(dataSourceVo, dataSourceCopy);

        // Copy the points.
        for (DataPointVO dataPoint : dataPointDao.getDataPoints(dataSourceVo, null)) {
            DataPointVO dataPointCopy = dataPoint.copy();
            dataPointCopy.setId(Common.NEW_ID);
            dataPointCopy.setXid(dataPointDao.generateUniqueXid());
            dataPointCopy.setName(dataPoint.getName());
            dataPointCopy.setDataSourceId(dataSourceCopy.getId());
            dataPointCopy.setEnabled(dataPoint.isEnabled());
            dataPointCopy.getComments().clear();

            // Copy the event detectors
            for (PointEventDetectorVO ped : dataPointCopy.getEventDetectors()) {
                ped.setId(Common.NEW_ID);
                ped.njbSetDataPoint(dataPointCopy);
            }

            dataPointDao.saveDataPoint(dataPointCopy);

            // Copy permissions
            dataPointDao.copyPermissions(dataPoint.getId(), dataPointCopy.getId());
        }

        return dataSourceCopy.getId();
    }
}
