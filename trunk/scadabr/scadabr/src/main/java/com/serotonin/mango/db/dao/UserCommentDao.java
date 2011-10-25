/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.util.StringUtils;

/**
 *
 * @author aploese
 */
@Repository
public class UserCommentDao extends BaseDao {

    private EventDao eventDao;

    /**
     * Logs a user comment after validation.
     * 
     * @param eventId
     * @param comment
     * @return
     */
    public UserComment addUserComment(int typeId, int referenceId, User user, String comment) {
        if (StringUtils.isEmpty(comment)) {
            return null;
        }

        UserComment c = new UserComment();
        c.setComment(comment);
        c.setTs(System.currentTimeMillis());
        c.setUserId(user.getId());
        c.setUsername(user.getUsername());

        if (typeId == UserComment.TYPE_EVENT) {
            eventDao.insertEventComment(referenceId, c);
        } else if (typeId == UserComment.TYPE_POINT) {
            insertUserComment(UserComment.TYPE_POINT, referenceId, c);
        } else {
            throw new ShouldNeverHappenException("Invalid comment type: " + typeId);
        }

        return c;
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

    void populateComments(List<EventInstance> list) {
        for (EventInstance e : list) {
            populateComments(e);
        }
    }

    //Todo replace string cat
    void populateComments(DataPointVO dp) {
        final String POINT_COMMENT_SELECT = UserCommentRowMapper.USER_COMMENT_SELECT
                + "where uc.commentType= "
                + UserComment.TYPE_POINT
                + " and uc.typeKey=? "
                + "order by uc.ts";

        dp.setComments(getJdbcTemplate().query(POINT_COMMENT_SELECT, new UserCommentRowMapper(), dp.getId()));
    }

    void populateComments(EventInstance event) {
        final String EVENT_COMMENT_SELECT = UserCommentDao.UserCommentRowMapper.USER_COMMENT_SELECT
                + "where uc.commentType= "
                + UserComment.TYPE_EVENT
                + " and uc.typeKey=? "
                + "order by uc.ts";

        event.setEventComments(getJdbcTemplate().query(EVENT_COMMENT_SELECT,
                new UserCommentRowMapper(), event.getId()));
    }

    /**
     * @return the eventDao
     */
    public EventDao getEventDao() {
        return eventDao;
    }

    /**
     * @param eventDao the eventDao to set
     */
    @Autowired
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    class UserCommentRowMapper implements ParameterizedRowMapper<UserComment> {

        static final String USER_COMMENT_SELECT = "select uc.userId, u.username, uc.ts, uc.commentText "
                + "from userComments uc left join users u on uc.userId = u.id ";

        @Override
        public UserComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserComment c = new UserComment();
            c.setUserId(rs.getInt(1));
            c.setUsername(rs.getString(2));
            c.setTs(rs.getLong(3));
            c.setComment(rs.getString(4));
            return c;
        }
    }
}
