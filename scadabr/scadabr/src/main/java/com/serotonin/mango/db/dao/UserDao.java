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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.mango.vo.permission.DataPointAccess;

public class UserDao extends BaseDao {

    private static final String USER_SELECT = "select id, username, password, email, phone, admin, disabled, selectedWatchList, homeUrl, lastLogin, "
            + "  receiveAlarmEmails, receiveOwnAuditEvents " + "from users ";

    public User getUser(int id) {
        User user = getSimpleJdbcTemplate().queryForObject(USER_SELECT + "where id=?", new UserRowMapper(), id);
        populateUserPermissions(user);
        return user;
    }

    public User getUser(String username) {
        User user = getSimpleJdbcTemplate().queryForObject(USER_SELECT + "where lower(username)=?", new UserRowMapper(), username.toLowerCase());
        populateUserPermissions(user);
        return user;
    }

    class UserRowMapper implements ParameterizedRowMapper<User> {

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            int i = 0;
            user.setId(rs.getInt(++i));
            user.setUsername(rs.getString(++i));
            user.setPassword(rs.getString(++i));
            user.setEmail(rs.getString(++i));
            user.setPhone(rs.getString(++i));
            user.setAdmin(charToBool(rs.getString(++i)));
            user.setDisabled(charToBool(rs.getString(++i)));
            user.setSelectedWatchList(rs.getInt(++i));
            user.setHomeUrl(rs.getString(++i));
            user.setLastLogin(rs.getLong(++i));
            user.setReceiveAlarmEmails(AlarmLevels.fromMangoId(rs.getInt(++i)));
            user.setReceiveOwnAuditEvents(charToBool(rs.getString(++i)));
            return user;
        }
    }

    public List<User> getUsers() {
        List<User> users = getSimpleJdbcTemplate().query(USER_SELECT + "order by username", new UserRowMapper());
        populateUserPermissions(users);
        return users;
    }

    public List<User> getActiveUsers() {
        List<User> users = getSimpleJdbcTemplate().query(USER_SELECT + "where disabled=?", new UserRowMapper(), boolToChar(false));
        populateUserPermissions(users);
        return users;
    }

    private void populateUserPermissions(List<User> users) {
        for (User user : users) {
            populateUserPermissions(user);
        }
    }
    private static final String SELECT_DATA_SOURCE_PERMISSIONS = "select dataSourceId from dataSourceUsers where userId=?";
    private static final String SELECT_DATA_POINT_PERMISSIONS = "select dataPointId, permission from dataPointUsers where userId=?";

    public void populateUserPermissions(User user) {
        if (user == null) {
            return;
        }

        user.setDataSourcePermissions(getJdbcTemplate().queryForList(SELECT_DATA_SOURCE_PERMISSIONS, new Object[]{user.getId()}, Integer.class));
        user.setDataPointPermissions(getSimpleJdbcTemplate().query(SELECT_DATA_POINT_PERMISSIONS, new ParameterizedRowMapper<DataPointAccess>() {

            @Override
            public DataPointAccess mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataPointAccess a = new DataPointAccess();
                a.setDataPointId(rs.getInt(1));
                a.setPermission(rs.getInt(2));
                return a;
            }
        }, user.getId()));
    }

    public void saveUser(final User user) {
        new TransactionTemplate(getTransactionManager()).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (user.getId() == Common.NEW_ID) {
                    insertUser(user);
                } else {
                    updateUser(user);
                }
            }
        });
    }
    private static final String USER_INSERT = "insert into users ("
            + "  username, password, email, phone, admin, disabled, homeUrl, receiveAlarmEmails, receiveOwnAuditEvents) "
            + "values (?,?,?,?,?,?,?,?,?)";

    void insertUser(User user) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("users").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
        params.put("phone", user.getPhone());
        params.put("admin", boolToChar(user.isAdmin()));
        params.put("disabled", boolToChar(user.isDisabled()));
        params.put("homeUrl", user.getHomeUrl());
        params.put("receiveAlarmEmails", user.getReceiveAlarmEmails().mangoId);
        params.put("receiveOwnAuditEvents", boolToChar(user.isReceiveOwnAuditEvents()));

        Number id = insertActor.executeAndReturnKey(params);

        user.setId(id.intValue());

        saveRelationalData(user);
    }
    private static final String USER_UPDATE = "update users set "
            + "  username=?, password=?, email=?, phone=?, admin=?, disabled=?, homeUrl=?, receiveAlarmEmails=?, "
            + "  receiveOwnAuditEvents=? " + "where id=?";

    void updateUser(User user) {
        getSimpleJdbcTemplate().update(
                USER_UPDATE,
                user.getUsername(), user.getPassword(), user.getEmail(), user.getPhone(),
                boolToChar(user.isAdmin()), boolToChar(user.isDisabled()), user.getHomeUrl(),
                user.getReceiveAlarmEmails().mangoId, boolToChar(user.isReceiveOwnAuditEvents()), user.getId());
        saveRelationalData(user);
    }

    private void saveRelationalData(final User user) {
        // Delete existing permissions.
        getSimpleJdbcTemplate().update("delete from dataSourceUsers where userId=?", user.getId());
        getSimpleJdbcTemplate().update("delete from dataPointUsers where userId=?", user.getId());

        // Save the new ones.
        getJdbcTemplate().batchUpdate("insert into dataSourceUsers (dataSourceId, userId) values (?,?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public int getBatchSize() {
                        return user.getDataSourcePermissions().size();
                    }

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, user.getDataSourcePermissions().get(i));
                        ps.setInt(2, user.getId());
                    }
                });
        getJdbcTemplate().batchUpdate("insert into dataPointUsers (dataPointId, userId, permission) values (?,?,?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public int getBatchSize() {
                        return user.getDataPointPermissions().size();
                    }

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, user.getDataPointPermissions().get(i).getDataPointId());
                        ps.setInt(2, user.getId());
                        ps.setInt(3, user.getDataPointPermissions().get(i).getPermission());
                    }
                });
    }

    public void deleteUser(final int userId) {
        new TransactionTemplate(getTransactionManager()).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                getSimpleJdbcTemplate().update("update userComments set userId=null where userId=?", userId);
                getSimpleJdbcTemplate().update("delete from mailingListMembers where userId=?", userId);
                getSimpleJdbcTemplate().update("update pointValueAnnotations set sourceId=null where sourceId=? and sourceType="
                        + SetPointSource.Types.USER, userId);
                getSimpleJdbcTemplate().update("delete from userEvents where userId=?", userId);
                getSimpleJdbcTemplate().update("update events set ackUserId=null, alternateAckSource="
                        + EventInstance.AlternateAcknowledgementSources.DELETED_USER + " where ackUserId=?", userId);
                getSimpleJdbcTemplate().update("delete from users where id=?", userId);
            }
        });
    }

    public void recordLogin(int userId) {
        getSimpleJdbcTemplate().update("update users set lastLogin=? where id=?", System.currentTimeMillis(), userId);
    }

    public void saveHomeUrl(int userId, String homeUrl) {
        getSimpleJdbcTemplate().update("update users set homeUrl=? where id=?", homeUrl, userId);
    }

    public void insertUserComment(int typeId, int referenceId, UserComment comment) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("userComments");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", comment.getUserId());
        params.put("commentType", typeId);
        params.put("typeKey", referenceId);
        params.put("ts", comment.getTs());
        params.put("commentText", comment.getComment());

        insertActor.execute(params);
    }
}
