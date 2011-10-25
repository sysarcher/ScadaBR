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
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.mango.Common;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.WatchList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Matthew Lohbihler
 */
@Service
public class WatchListDao extends BaseDao {

    @Autowired
    private DataPointDao dataPointDao;

    public String generateUniqueXid() {
        return generateUniqueXid(WatchList.XID_PREFIX, "watchLists");
    }

    public boolean isXidUnique(String xid, int watchListId) {
        return isXidUnique(xid, watchListId, "watchLists");
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchList> getWatchLists(final int userId) {
        return getSimpleJdbcTemplate().query("select id, xid, userId, name from watchLists " //
                + "where userId=? or id in (select watchListId from watchListUsers where userId=?) " //
                + "order by name", new WatchListRowMapper(), userId, userId);
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchList> getWatchLists() {
        return getSimpleJdbcTemplate().query("select id, xid, userId, name from watchLists", new WatchListRowMapper());
    }

    public WatchList getWatchList(int watchListId) {
        // Get the watch lists.
        WatchList watchList = getSimpleJdbcTemplate().queryForObject("select id, xid, userId, name from watchLists where id=?", new WatchListRowMapper(), watchListId);
        populateWatchlistData(watchList);
        return watchList;
    }

    public void populateWatchlistData(List<WatchList> watchLists) {
        for (WatchList watchList : watchLists) {
            populateWatchlistData(watchList);
        }
    }

    public void populateWatchlistData(WatchList watchList) {
        if (watchList == null) {
            return;
        }

        // Get the points for each of the watch lists.
        List<Integer> pointIds = getJdbcTemplate().queryForList(
                "select dataPointId from watchListPoints where watchListId=? order by sortOrder",
                new Object[]{watchList.getId()}, Integer.class);
        List<DataPointVO> points = watchList.getPointList();
        for (Integer pointId : pointIds) {
            points.add(dataPointDao.getDataPoint(pointId));
        }

        setWatchListUsers(watchList);
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public WatchList getWatchList(String xid) {
        return getSimpleJdbcTemplate().queryForObject("select id, xid, userId, name from watchLists where xid=?", new WatchListRowMapper(), xid);
    }

    class WatchListRowMapper implements ParameterizedRowMapper<WatchList> {

        @Override
        public WatchList mapRow(ResultSet rs, int rowNum) throws SQLException {
            WatchList wl = new WatchList();
            wl.setId(rs.getInt(1));
            wl.setXid(rs.getString(2));
            wl.setUserId(rs.getInt(3));
            wl.setName(rs.getString(4));
            return wl;
        }
    }

    public void saveSelectedWatchList(int userId, int watchListId) {
        getSimpleJdbcTemplate().update("update users set selectedWatchList=? where id=?", watchListId, userId);
    }

    //TODO call saveWatchlist???
    @Deprecated
    public WatchList createNewWatchList(WatchList watchList, int userId) {
        watchList.setUserId(userId);
        watchList.setXid(generateUniqueXid());

        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("watchLists").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", watchList.getXid());
        params.put("userId", watchList.getUserId());
        params.put("name", watchList.getName());

        Number id = insertActor.executeAndReturnKey(params);
        watchList.setId(id.intValue());

        return watchList;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void saveWatchList(final WatchList watchList) {
        if (watchList.getId() == Common.NEW_ID) {
            SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("watchLists").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("xid", watchList.getXid());
            params.put("userId", watchList.getUserId());
            params.put("name", watchList.getName());

            Number id = insertActor.executeAndReturnKey(params);
            watchList.setId(id.intValue());
        } else {
            getSimpleJdbcTemplate().update("update watchLists set xid=?, name=? where id=?", watchList.getXid(),
                    watchList.getName(), watchList.getId());
        }
        getSimpleJdbcTemplate().update("delete from watchListPoints where watchListId=?", watchList.getId());
        getJdbcTemplate().batchUpdate("insert into watchListPoints values (?,?,?)", new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return watchList.getPointList().size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, watchList.getId());
                ps.setInt(2, watchList.getPointList().get(i).getId());
                ps.setInt(3, i);
            }
        });

        saveWatchListUsers(watchList);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteWatchList(final int watchListId) {
        deleteWatchListUsers(watchListId);
        getSimpleJdbcTemplate().update("delete from watchListPoints where watchListId=?", watchListId);
        getSimpleJdbcTemplate().update("delete from watchLists where id=?", watchListId);
    }

    //
    //
    // Watch list users
    //
    private void setWatchListUsers(WatchList watchList) {
        watchList.setWatchListUsers(getSimpleJdbcTemplate().query("select userId, accessType from watchListUsers where watchListId=?", new WatchListUserRowMapper(), watchList.getId()));
    }

    class WatchListUserRowMapper implements ParameterizedRowMapper<ShareUser> {

        @Override
        public ShareUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareUser wlu = new ShareUser();
            wlu.setUserId(rs.getInt(1));
            wlu.setAccessType(rs.getInt(2));
            return wlu;
        }
    }

    void deleteWatchListUsers(int watchListId) {
        getSimpleJdbcTemplate().update("delete from watchListUsers where watchListId=?", watchListId);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void saveWatchListUsers(final WatchList watchList) {
        // Delete anything that is currently there.
        deleteWatchListUsers(watchList.getId());

        // Add in all of the entries.
        getJdbcTemplate().batchUpdate("insert into watchListUsers values (?,?,?)", new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return watchList.getWatchListUsers().size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ShareUser wlu = watchList.getWatchListUsers().get(i);
                ps.setInt(1, watchList.getId());
                ps.setInt(2, wlu.getUserId());
                ps.setInt(3, wlu.getAccessType());
            }
        });
    }

    public void removeUserFromWatchList(int watchListId, int userId) {
        getSimpleJdbcTemplate().update("delete from watchListUsers where watchListId=? and userId=?", watchListId, userId);
    }
}
