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

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.serotonin.mango.Common;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.view.View;
import com.serotonin.mango.vo.User;
import com.serotonin.util.SerializationHelper;

public class ViewDao extends BaseDao {
    //
    // /
    // / Views
    // /
    //

    private static final String VIEW_SELECT = "select data, id, xid, name, background, userId, anonymousAccess from mangoViews";
    private static final String USER_ID_COND = " where userId=? or id in (select mangoViewId from mangoViewUsers where userId=?)";

    public List<View> getViews() {
        List<View> views = getSimpleJdbcTemplate().query(VIEW_SELECT, new ViewRowMapper());
        setViewUsers(views);
        return views;
    }

    public List<View> getViews(int userId) {
        List<View> views = getSimpleJdbcTemplate().query(VIEW_SELECT + USER_ID_COND, new ViewRowMapper(), userId, userId);
        setViewUsers(views);
        return views;
    }

    public Map<Integer, String> getViewNames(User user) {
        return (Map<Integer, String>) getJdbcTemplate().query("select id, name from mangoViews" + USER_ID_COND, new Object[]{user.getId(), user.getId()}, new ResultSetExtractor() {

            @Override
            public Map<Integer, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<Integer, String> result = new LinkedHashMap<Integer, String>();
                while (rs.next()) {
                    result.put(rs.getInt("id"), rs.getString("name"));
                }
                return result;
            }
        });
    }

    private void setViewUsers(List<View> views) {
        for (View view : views) {
            setViewUsers(view);
        }
    }

    public View getView(int id) {
        return getSingleView(VIEW_SELECT + " where id=?", id);
    }

    public View getViewByXid(String xid) {
        return getSingleView(VIEW_SELECT + " where xid=?", xid);
    }

    public View getView(String name) {
        return getSingleView(VIEW_SELECT + " where name=?", name);
    }

    private View getSingleView(String sql, Object param) {
        View view = getSimpleJdbcTemplate().queryForObject(sql, new ViewRowMapper(), param);
        if (view == null) {
            return null;
        }

        setViewUsers(view);
        return view;
    }

    class ViewRowMapper implements ParameterizedRowMapper<View> {

        @Override
        public View mapRow(ResultSet rs, int rowNum) throws SQLException {
            View v;
            Blob blob = rs.getBlob(1);
            if (blob == null) // This can happen during upgrade
            {
                v = new View();
            } else {
                v = (View) SerializationHelper.readObject(blob.getBinaryStream());
            }

            v.setId(rs.getInt(2));
            v.setXid(rs.getString(3));
            v.setName(rs.getString(4));
            v.setBackgroundFilename(rs.getString(5));
            v.setUserId(rs.getInt(6));
            v.setAnonymousAccess(rs.getInt(7));

            return v;
        }
    }

    class ViewNameRowMapper implements ParameterizedRowMapper<View> {

        @Override
        public View mapRow(ResultSet rs, int rowNum) throws SQLException {
            View v = new View();
            v.setId(rs.getInt(1));
            v.setName(rs.getString(2));
            v.setUserId(rs.getInt(3));

            return v;
        }
    }

    public String generateUniqueXid() {
        return generateUniqueXid(View.XID_PREFIX, "mangoViews");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "mangoViews");
    }

    public void saveView(final View view) {
        new TransactionTemplate(getTransactionManager()).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // Decide whether to insert or update.
                if (view.getId() == Common.NEW_ID) {
                    insertView(view);
                } else {
                    updateView(view);
                }

                saveViewUsers(view);
            }
        });
    }

    void insertView(View view) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("pointHierarchy").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", view.getXid());
        params.put("name", view.getName());
        params.put("background", view.getBackgroundFilename());
        params.put("userId", view.getUserId());
        params.put("anonymousAccess", view.getAnonymousAccess());
        params.put("data", SerializationHelper.writeObjectToArray(view));

        Number id = insertActor.executeAndReturnKey(params);
        view.setId(id.intValue());
    }

    void updateView(View view) {
        getSimpleJdbcTemplate().update("update mangoViews set xid=?, name=?, background=?, anonymousAccess=?, data=? where id=?",
                view.getXid(), view.getName(), view.getBackgroundFilename(), view.getAnonymousAccess(),
                SerializationHelper.writeObjectToArray(view), view.getId());
    }

    public void removeView(final int viewId) {
        deleteViewUsers(viewId);
        getSimpleJdbcTemplate().update("delete from mangoViews where id=?", viewId);
    }

    //
    // /
    // / View users
    // /
    //
    private void setViewUsers(View view) {
        view.setViewUsers(getSimpleJdbcTemplate().query("select userId, accessType from mangoViewUsers where mangoViewId=?",
                new ViewUserRowMapper(), view.getId()));
    }

    class ViewUserRowMapper implements ParameterizedRowMapper<ShareUser> {

        @Override
        public ShareUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareUser vu = new ShareUser();
            vu.setUserId(rs.getInt(1));
            vu.setAccessType(rs.getInt(2));
            return vu;
        }
    }

    private void deleteViewUsers(int viewId) {
        getSimpleJdbcTemplate().update("delete from mangoViewUsers where mangoViewId=?", viewId);
    }

    void saveViewUsers(final View view) {
        // Delete anything that is currently there.
        deleteViewUsers(view.getId());

        // Add in all of the entries.
        getJdbcTemplate().batchUpdate("insert into mangoViewUsers values (?,?,?)", new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return view.getViewUsers().size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ShareUser vu = view.getViewUsers().get(i);
                ps.setInt(1, view.getId());
                ps.setInt(2, vu.getUserId());
                ps.setInt(3, vu.getAccessType());
            }
        });
    }

    public void removeUserFromView(int viewId, int userId) {
        getSimpleJdbcTemplate().update("delete from mangoViewUsers where mangoViewId=? and userId=?", viewId, userId);
    }
}
