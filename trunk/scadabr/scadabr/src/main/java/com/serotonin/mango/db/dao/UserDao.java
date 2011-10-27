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

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.DataPointAccess;
import java.util.Date;
import org.springframework.transaction.annotation.Propagation;

@Repository
@Transactional(readOnly = true)
public class UserDao extends BaseDao {

    public class UsersMappingQuery extends MappingSqlQuery<User> {

        public UsersMappingQuery(DataSource ds, String sql) {
            super(ds, sql);
            compile();
        }
        private UserRowMapper userRowMapper = new UserRowMapper();

        @Override
        protected User mapRow(ResultSet rs, int i) throws SQLException {
            return userRowMapper.mapRow(rs, i);
        }
    }
    private UsersMappingQuery selectUsers;

    @PostConstruct
    public void init() {
        selectUsers = new UsersMappingQuery(getDataSource(), UserRowMapper.USER_SELECT + "order by mangoUserName"); // + " order by username");
    }

    public User getUser(int id) {
        try {
            User user = getJdbcTemplate().queryForObject(UserRowMapper.USER_SELECT + "where id=?", new UserRowMapper(), id);
            populateUserPermissions(user);
            return user;
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public User getUser(String username) {
        try {
            User user = getJdbcTemplate().queryForObject(UserRowMapper.USER_SELECT + "where lower(username)=?", new UserRowMapper(), username.toLowerCase());
            populateUserPermissions(user);
            return user;
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    class UserRowMapper implements ParameterizedRowMapper<User> {

        static final String USER_SELECT =
                "select "
                + " id, "
                + " mangoUsername, "
                + " mangoUserPassword, "
                + " email, "
                + " phone, "
                + " mangoAdmin, "
                + " disabled, "
                + " selectedWatchList, "
                + " homeUrl, "
                + " lastLogin, "
                + " receiveAlarmEmails, "
                + " receiveOwnAuditEvents "
                + "from users ";

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("mangoUserName"));
            user.setPassword(rs.getString("mangoUserPassword"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setAdmin(rs.getBoolean("mangoAdmin"));
            user.setDisabled(rs.getBoolean("disabled"));
            user.setSelectedWatchList(rs.getInt("selectedWatchList"));
            user.setHomeUrl(rs.getString("homeUrl"));
            System.err.println(rs.getString("lastLogin"));
            Long ts = rs.getLong("lastLogin");
            user.setLastLogin(ts != null ? new Date(ts) : null);
            user.setReceiveAlarmEmails(AlarmLevels.valueOf(rs.getString("receiveAlarmEmails")));
            user.setReceiveOwnAuditEvents(rs.getBoolean("receiveOwnAuditEvents"));
            return user;
        }
    }

    public List<User> getUsers() {
        List<User> users = null;
        try {
            users = selectUsers.execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        populateUserPermissions(users);
        return users;
    }

    public List<User> getActiveUsers() {
        List<User> users = getJdbcTemplate().query(UserRowMapper.USER_SELECT + "where disabled=?", new UserRowMapper(), false);
        populateUserPermissions(users);
        return users;
    }

    private void populateUserPermissions(List<User> users) {
        for (User user : users) {
            populateUserPermissions(user);
        }
    }

    @Transactional(readOnly = true)
    public void populateUserPermissions(User user) {
        if (user == null) {
            return;
        }

        user.setDataSourcePermissions(getJdbcTemplate().queryForList("select dataSourceId from dataSourceUsers where userId=?", new Object[]{user.getId()}, Integer.class));
        user.setDataPointPermissions(getJdbcTemplate().query("select dataPointId, dataPointUserPermission from dataPointUsers where userId=?", new ParameterizedRowMapper<DataPointAccess>() {

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
        if (user.getId() == Common.NEW_ID) {
            insertUser(user);
        } else {
            updateUser(user);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    private void insertUser(User user) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("users").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap();
        params.put("mangoUsername", user.getUsername());
        params.put("mangoUserPassword", user.getPassword());
        params.put("email", user.getEmail());
        params.put("phone", user.getPhone());
        params.put("mangoAdmin", user.isAdmin());
        params.put("disabled", user.isDisabled());
        params.put("homeUrl", user.getHomeUrl());
        params.put("receiveAlarmEmails", user.getReceiveAlarmEmails());
        params.put("receiveOwnAuditEvents", user.isReceiveOwnAuditEvents());

        Number id = insertActor.executeAndReturnKey(params);

        user.setId(id.intValue());

        saveRelationalData(user);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    private void updateUser(User user) {
        final String UPDATE_SQL =
                "update users set "
                + " mangoUsername=:mangoUsername,"
                + " mangoUserPassword=:mangoUserPassword,"
                + " email=:email,"
                + " phone=:phone,"
                + " mangoAdmin=mangoAdmin,"
                + " disabled=:disabled,"
                + " homeUrl=:homeUrl,"
                + " receiveAlarmEmails=:receiveAlarmEmails,"
                + " receiveOwnAuditEvents=:receiveOwnAuditEvents "
                + " where id=:id";
        Map<String, Object> params = new HashMap();
        params.put("mangoUsername", user.getUsername());
        params.put("mangoUserPassword", user.getPassword());
        params.put("email", user.getEmail());
        params.put("phone", user.getPhone());
        params.put("mangoAdmin", user.isAdmin());
        params.put("disabled", user.isDisabled());
        params.put("homeUrl", user.getHomeUrl());
        params.put("receiveAlarmEmails", user.getReceiveAlarmEmails().name());
        params.put("receiveOwnAuditEvents", user.isReceiveOwnAuditEvents());
        params.put("id", user.getId());
        getSimpleJdbcTemplate().update(
                UPDATE_SQL, params);

        saveRelationalData(user);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void saveRelationalData(final User user) {
        // Delete existing permissions. 
        getSimpleJdbcTemplate().update("delete from dataSourceUsers where userId=?", user.getId());
        getSimpleJdbcTemplate().update("delete from dataPointUsers where userId=?", user.getId());

        if (user.getDataSourcePermissions().isEmpty()) {
            return;
        }

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

    /**
     * Delete user from database.
     * First clean upn any references (usercomments, pointValueAnnotations and events) and then delete user
     * @param user the user to delete
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteUser(final User user) {
        String errorMessage = "";
        //TODO check if user has ... and if yes then stop deleting
        if (getSimpleJdbcTemplate().queryForInt("select count(*) from eventComments where userId=?", user.getId()) > 0) {
            errorMessage += "User has eventComments\n";
        }
        if (getSimpleJdbcTemplate().queryForInt("select count(*) from dataPointComments where userId=?", user.getId()) > 0) {
            errorMessage += "User has dataPointComments\n";
        }
        if (getSimpleJdbcTemplate().queryForInt("select count(*) from pointValueAnnotations where sourceId=? and sourceType=? ", user.getId(), SetPointSource.Types.USER) > 0) {
            errorMessage += "User has pointValueAnnotations\n";
        }
        if (getSimpleJdbcTemplate().queryForInt("select count(*) from events where ackUserId=?", user.getId()) > 0) {
            errorMessage += "User has acknownledget events\n";
        }
        //TODO implement caller an Localization
        if (errorMessage.length() > 0) {
            throw new RuntimeException(errorMessage);
        }
        getSimpleJdbcTemplate().update("delete from users where id=?", user.getId());
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void saveLastLogin(final User user) {
        getJdbcTemplate().update("update users set lastLogin=? where id=?", user.getLastLogin().getTime(), user.getId()); 
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void saveHomeUrl(User user, String homeUrl) {
        getSimpleJdbcTemplate().update("update users set homeUrl=? where id=?", homeUrl, user.getId());
    }
}
