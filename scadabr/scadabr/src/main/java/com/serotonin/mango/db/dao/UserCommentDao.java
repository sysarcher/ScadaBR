/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.DataPointComment;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.EventComment;

/**
 *
 * @author aploese
 * This class habndles dataPointComments and eventComments
 */
@Repository
public class UserCommentDao extends BaseDao {

    class EventCommentRowMapper implements ParameterizedRowMapper<EventComment> {

        static final String USER_COMMENT_SELECT = "select ec.userId, u.mangoUserame, ec.ts, ec.commentText ec.eventId "
                + "from eventComments ec left join users u on ec.userId = u.id ";

        @Override
        public EventComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventComment result = new EventComment();
            result.setUserId(rs.getInt("userId"));
            result.setUsername(rs.getString("mangoUsename"));
            result.setEventId(rs.getInt("eventId"));
            result.setTs(rs.getDate("ts"));
            result.setComment(rs.getString("commentText"));
            return result;
        }
    }

    class DataPointCommentRowMapper implements ParameterizedRowMapper<DataPointComment> {

        static final String USER_COMMENT_SELECT = "select dpc.userId, u.mangoUserame, dpc.ts, dpc.commentText dpc.dataPointId "
                + "from dataPointComments dpc left join users u on ec.userId = u.id ";

        @Override
        public DataPointComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            DataPointComment result = new DataPointComment();
            result.setUserId(rs.getInt("userId"));
            result.setUsername(rs.getString("mangoUsename"));
            result.setDataPointId(rs.getInt("dataPointId"));
            result.setTs(rs.getDate("ts"));
            result.setComment(rs.getString("comment"));
            return result;
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void insertComment(DataPointComment comment) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("dataPointComments");
        Map<String, Object> params = new HashMap();
        params.put("userId", comment.getUserId());
        params.put("dataPointId", comment.getDataPointId());
        params.put("ts", comment.getTs());
        params.put("commentText", comment.getComment());

        insertActor.execute(params);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void insertComment(EventComment comment) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("eventComments");
        Map<String, Object> params = new HashMap();
        params.put("userId", comment.getUserId());
        params.put("eventId", comment.getEventId());
        params.put("ts", comment.getTs());
        params.put("commentText", comment.getComment());

        insertActor.execute(params);
    }

    void populateComments(List<EventInstance> list) {
        for (EventInstance e : list) {
            populateComments(e);
        }
    }

    @Transactional(readOnly = true)
    void populateComments(DataPointVO dp) {
        final String POINT_COMMENT_SELECT = DataPointCommentRowMapper.USER_COMMENT_SELECT
                + "where dpc.dataPointId= :dataPointId "
                + "order by uc.ts";
        Map<String, Integer> params = Collections.singletonMap("dataPointId", dp.getId());

        dp.setComments(getJdbcTemplate().query(POINT_COMMENT_SELECT, new DataPointCommentRowMapper(), params));
    }

    @Transactional(readOnly = true)
    void populateComments(EventInstance event) {
        final String EVENT_COMMENT_SELECT = EventCommentRowMapper.USER_COMMENT_SELECT
                + "where ec.eventId= :eventId "
                + "order by uc.ts";
        Map<String, Integer> params = Collections.singletonMap("eventId", event.getId());
        event.setEventComments(getJdbcTemplate().query(EVENT_COMMENT_SELECT, new EventCommentRowMapper(), params));
    }
}
