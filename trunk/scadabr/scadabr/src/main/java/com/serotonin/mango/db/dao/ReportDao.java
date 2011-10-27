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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.ImageValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.EventComment;
import com.serotonin.mango.vo.report.ReportDataStreamHandler;
import com.serotonin.mango.vo.report.ReportDataValue;
import com.serotonin.mango.vo.report.ReportInstance;
import com.serotonin.mango.vo.report.ReportPointInfo;
import com.serotonin.mango.vo.report.ReportUserComment;
import com.serotonin.mango.vo.report.ReportVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.I18NUtils;
import com.serotonin.web.taglib.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Matthew Lohbihler
 */
@Service
public class ReportDao extends BaseDao {
    
    @Autowired
    private PointValueDao pointValueDao;
    //
    //
    // Report Templates
    //

    private static final String REPORT_SELECT = "select data, id, userId, name from reports ";

    public List<ReportVO> getReports() {
        return getSimpleJdbcTemplate().query(REPORT_SELECT, new ReportRowMapper());
    }

    public List<ReportVO> getReports(int userId) {
        return getSimpleJdbcTemplate().query(REPORT_SELECT + "where userId=? order by name", new ReportRowMapper(), userId);
    }

    public ReportVO getReport(int id) {
        return getSimpleJdbcTemplate().queryForObject(REPORT_SELECT + "where id=?", new ReportRowMapper(), id);
    }

    class ReportRowMapper implements ParameterizedRowMapper<ReportVO> {

        @Override
        public ReportVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            ReportVO report = (ReportVO) SerializationHelper.readObject(rs.getBlob(++i).getBinaryStream());
            report.setId(rs.getInt(++i));
            report.setUserId(rs.getInt(++i));
            report.setName(rs.getString(++i));
            return report;
        }
    }

    public void saveReport(ReportVO report) {
        if (report.getId() == Common.NEW_ID) {
            insertReport(report);
        } else {
            updateReport(report);
        }
    }

    private void insertReport(final ReportVO report) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("reports").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap();
        params.put("userId", report.getUserId());
        params.put("name", report.getName());
        params.put("data", SerializationHelper.writeObjectToArray(report));

        Number id = insertActor.executeAndReturnKey(params);
        report.setId(id.intValue());
    }
    private static final String REPORT_UPDATE = "update reports set userId=?, name=?, data=? where id=?";

    private void updateReport(final ReportVO report) {
        getSimpleJdbcTemplate().update(
                REPORT_UPDATE,
                report.getUserId(), report.getName(), SerializationHelper.writeObjectToArray(report), report.getId());
    }

    public void deleteReport(int reportId) {
        getSimpleJdbcTemplate().update("delete from reports where id=?", reportId);
    }
    //
    //
    // Report Instances
    //
    private static final String REPORT_INSTANCE_SELECT = "select id, userId, name, includeEvents, includeUserComments, reportStartTime, reportEndTime, runStartTime, "
            + "  runEndTime, recordCount, preventPurge " + "from reportInstances ";

    public List<ReportInstance> getReportInstances(int userId) {
        return getSimpleJdbcTemplate().query(REPORT_INSTANCE_SELECT + "where userId=? order by runStartTime desc", new ReportInstanceRowMapper(), userId);
    }

    public ReportInstance getReportInstance(int id) {
        return getSimpleJdbcTemplate().queryForObject(REPORT_INSTANCE_SELECT + "where id=?", new ReportInstanceRowMapper(), id);
    }

    class ReportInstanceRowMapper implements ParameterizedRowMapper<ReportInstance> {

        @Override
        public ReportInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            ReportInstance ri = new ReportInstance();
            ri.setId(rs.getInt(++i));
            ri.setUserId(rs.getInt(++i));
            ri.setName(rs.getString(++i));
            ri.setIncludeEvents(rs.getInt(++i));
            ri.setIncludeUserComments(charToBool(rs.getString(++i)));
            ri.setReportStartTime(rs.getLong(++i));
            ri.setReportEndTime(rs.getLong(++i));
            ri.setRunStartTime(rs.getLong(++i));
            ri.setRunEndTime(rs.getLong(++i));
            ri.setRecordCount(rs.getInt(++i));
            ri.setPreventPurge(charToBool(rs.getString(++i)));
            return ri;
        }
    }

    public void deleteReportInstance(int id, int userId) {
        getSimpleJdbcTemplate().update("delete from reportInstances where id=? and userId=?", id, userId);
    }

    public int purgeReportsBefore(final long time) {
        return getSimpleJdbcTemplate().update("delete from reportInstances where runStartTime<? and preventPurge=?", time, boolToChar(false));
    }

    public void setReportInstancePreventPurge(int id, boolean preventPurge, int userId) {
        getSimpleJdbcTemplate().update("update reportInstances set preventPurge=? where id=? and userId=?", boolToChar(preventPurge), id, userId);
    }
    /**
     * This method should only be called by the ReportWorkItem.
     */
    private static final String REPORT_INSTANCE_INSERT = "insert into reportInstances "
            + "  (userId, name, , , , , , "
            + "     runEndTime, , ) " + "  values (?,?,?,?,?,?,?,?,?,?)";
    private static final String REPORT_INSTANCE_UPDATE = "update reportInstances set reportStartTime=?, reportEndTime=?, runStartTime=?, runEndTime=?, recordCount=? "
            + "where id=?";

    public void saveReportInstance(ReportInstance instance) {
        if (instance.getId() == Common.NEW_ID) {
            SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("reportInstances").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap();
            params.put("userId", instance.getUserId());
            params.put("name", instance.getName());
            params.put("includeEvents", instance.getIncludeEvents());
            params.put("includeUserComments", boolToChar(instance.isIncludeUserComments()));
            params.put("reportStartTime", instance.getReportStartTime());
            params.put("reportEndTime", instance.getReportEndTime());
            params.put("runStartTime", instance.getRunStartTime());
            params.put("runEndTime", instance.getRunStartTime());
            params.put("recordCount", instance.getRunEndTime());
            params.put("preventPurge", boolToChar(instance.isPreventPurge()));

            Number reportPointId = insertActor.executeAndReturnKey(params);

            instance.setId(reportPointId.intValue());
        } else {
            getSimpleJdbcTemplate().update(
                    REPORT_INSTANCE_UPDATE,
                    instance.getReportStartTime(), instance.getReportEndTime(),
                    instance.getRunStartTime(), instance.getRunEndTime(), instance.getRecordCount(),
                    instance.getId());
        }
    }

    /**
     * This method should only be called by the ReportWorkItem.
     */
    public static class PointInfo {

        private final DataPointVO point;
        private final String colour;
        private final boolean consolidatedChart;

        public PointInfo(DataPointVO point, String colour, boolean consolidatedChart) {
            this.point = point;
            this.colour = colour;
            this.consolidatedChart = consolidatedChart;
        }

        public DataPointVO getPoint() {
            return point;
        }

        public String getColour() {
            return colour;
        }

        public boolean isConsolidatedChart() {
            return consolidatedChart;
        }
    }

    public int runReport(final ReportInstance instance, List<PointInfo> points, ResourceBundle bundle) {
        int count = 0;
        String userLabel = I18NUtils.getMessage(bundle, "common.user");
        String setPointLabel = I18NUtils.getMessage(bundle, "annotation.eventHandler");
        String anonymousLabel = I18NUtils.getMessage(bundle, "annotation.anonymous");
        String deletedLabel = I18NUtils.getMessage(bundle, "common.deleted");

        // The timestamp selection code is used multiple times for different tables
        String timestampSql;
        Object[] timestampParams;
        if (instance.isFromInception() && instance.isToNow()) {
            timestampSql = "";
            timestampParams = new Object[0];
        } else if (instance.isFromInception()) {
            timestampSql = "and ${field}<?";
            timestampParams = new Object[]{instance.getReportEndTime()};
        } else if (instance.isToNow()) {
            timestampSql = "and ${field}>=?";
            timestampParams = new Object[]{instance.getReportStartTime()};
        } else {
            timestampSql = "and ${field}>=? and ${field}<?";
            timestampParams = new Object[]{instance.getReportStartTime(), instance.getReportEndTime()};
        }

        // For each point.
        for (PointInfo pointInfo : points) {
            DataPointVO point = pointInfo.getPoint();
            MangoDataType dataType = point.getPointLocator().getMangoDataType();

            MangoValue startValue = null;
            if (!instance.isFromInception()) {
                // Get the value just before the start of the report
                PointValueTime pvt = pointValueDao.getPointValueBefore(point.getId(), instance.getReportStartTime());
                if (pvt != null) {
                    startValue = pvt.getValue();
                }

                // Make sure the data types match
                if (startValue.getMangoDataType() != dataType) {
                    startValue = null;
                }
            }

            // Insert the reportInstancePoints record
            String name = Functions.truncate(point.getName(), 100);

            SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("reportInstancePoints").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap();
            params.put("reportInstanceId", instance.getId());
            params.put("dataSourceName", point.getDeviceName());
            params.put("pointName", name);
            params.put("dataType", dataType);
            params.put("startValue", startValue.getMangoDataType());
            params.put("textRenderer", SerializationHelper.writeObjectToArray(point.getTextRenderer()));
            params.put("colour", pointInfo.getColour());
            params.put("consolidatedChart", boolToChar(pointInfo.isConsolidatedChart()));

            Number reportPointId = insertActor.executeAndReturnKey(params);

            // Insert the reportInstanceData records
            String insertSQL = "insert into reportInstanceData " + "  select id, " + reportPointId.intValue()
                    + ", pointValue, ts from pointValues " + "    where dataPointId=? and dataType=? "
                    + StringUtils.replaceMacro(timestampSql, "field", "ts");
            count += getSimpleJdbcTemplate().update(insertSQL, appendParameters(timestampParams, point.getId(), dataType));

            String annoCase;
            switch (getDataBaseType()) {
                case DERBY: 
                annoCase = "    case when pva.sourceType=1 then '" + userLabel //
                        + ": ' || (case when u.username is null then '" + deletedLabel + "' else u.username end) " //
                        + "         when pva.sourceType=2 then '" + setPointLabel + "' " //
                        + "         when pva.sourceType=3 then '" + anonymousLabel + "' " //
                        + "         else 'Unknown source type: ' || cast(pva.sourceType as char(3)) " //
                        + "    end ";
            break;
                    case MSSQL: 
                annoCase = "    case pva.sourceType" //
                        + "        when 1 then '" + userLabel + ": ' + isnull(u.username, '" + deletedLabel + "') " //
                        + "        when 2 then '" + setPointLabel + "'" //
                        + "        when 3 then '" + anonymousLabel + "'" //
                        + "        else 'Unknown source type: ' + cast(pva.sourceType as nvarchar)" //
                        + "    end ";
            break;
                    case MYSQL:
                annoCase = "    case pva.sourceType" //
                        + "      when 1 then concat('" + userLabel + ": ',ifnull(u.username,'" + deletedLabel + "')) " //
                        + "      when 2 then '" + setPointLabel + "'" //
                        + "      when 3 then '" + anonymousLabel + "'" //
                        + "      else concat('Unknown source type: ', pva.sourceType)" //
                        + "    end ";
            break;
                    default:
                throw new ShouldNeverHappenException("unhandled database type: "
                        + getDataBaseType());
            }

            // Insert the reportInstanceDataAnnotations records
            getSimpleJdbcTemplate().update("insert into reportInstanceDataAnnotations " //
                    + "  (pointValueId, reportInstancePointId, textPointValueShort, textPointValueLong, sourceValue) " //
                    + "  select rd.pointValueId, rd.reportInstancePointId, pva.textPointValueShort, " //
                    + "    pva.textPointValueLong, " + annoCase + "  from reportInstanceData rd " //
                    + "    join reportInstancePoints rp on rd.reportInstancePointId = rp.id " //
                    + "    join pointValueAnnotations pva on rd.pointValueId = pva.pointValueId " //
                    + "    left join users u on pva.sourceType=1 and pva.sourceId = u.id " //
                    + "  where rp.id = ?", reportPointId);

            // Insert the reportInstanceEvents records for the point.
            if (instance.getIncludeEvents() != ReportVO.EVENTS_NONE) {
                String eventSQL = "insert into reportInstanceEvents " //
                        + "  (eventId, reportInstanceId, typeId, typeRef1, typeRef2, activeTs, rtnApplicable, rtnTs," //
                        + "   rtnCause, alarmLevel, message, ackTs, ackUsername, alternateAckSource)" //
                        + "  select e.id, " + instance.getId() + ", e.typeId, e.typeRef1, e.typeRef2, e.activeTs, " //
                        + "    e.rtnApplicable, e.rtnTs, e.rtnCause, e.alarmLevel, e.message, e.ackTs, u.username, " //
                        + "    e.alternateAckSource " //
                        + "  from events e join userEvents ue on ue.eventId=e.id " //
                        + "    left join users u on e.ackUserId=u.id " //
                        + "  where ue.userId=? " //
                        + "    and e.typeId=" //
                        + EventType.EventSources.DATA_POINT //
                        + "    and e.typeRef1=? ";

                if (instance.getIncludeEvents() == ReportVO.EVENTS_ALARMS) {
                    eventSQL += "and e.alarmLevel > 0 ";
                }

                eventSQL += StringUtils.replaceMacro(timestampSql, "field", "e.activeTs");
                getSimpleJdbcTemplate().update(eventSQL, appendParameters(timestampParams, instance.getUserId(), point.getId()));
            }

            //TODO cleanup SQL and use named params
            // Insert the reportInstanceUserComments records for the point.
            if (instance.isIncludeUserComments()) {
                String commentSQL = "insert into reportInstanceUserComments " //
                        + "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
//TODO                        + "  select " + instance.getId() + ", u.username, " + UserComment.CommentType.POINT.name() + ", " //
                        + reportPointId + ", uc.ts, uc.commentText " //
                        + "  from userComments uc " //
                        + "    left join users u on uc.userId=u.id " //
//TODO                        + "  where uc.commentType=" + UserComment.CommentType.POINT.name() //
                        + "    and uc.typeKey=? ";

                // Only include comments made in the duration of the report.
                commentSQL += StringUtils.replaceMacro(timestampSql, "field", "uc.ts");
                getSimpleJdbcTemplate().update(commentSQL, appendParameters(timestampParams, point.getId()));
            }
        }

        // Insert the reportInstanceUserComments records for the selected events
        if (instance.isIncludeUserComments()) {
            String commentSQL = "insert into reportInstanceUserComments " //
                    + "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
//TODO                    + "  select " + instance.getId() + ", u.username, " + UserComment.CommentType.EVENT.name() + ", uc.typeKey, " //
                    + "    uc.ts, uc.commentText " //
                    + "  from userComments uc " //
                    + "    left join users u on uc.userId=u.id " //
                    + "    join reportInstanceEvents re on re.eventId=uc.typeKey " //
//TODO                    + "  where uc.commentType=" + UserComment.CommentType.EVENT.name() //
                    + "    and re.reportInstanceId=? ";
            getSimpleJdbcTemplate().update(commentSQL, instance.getId());
        }

        // If the report had undefined start or end times, update them with values from the data.
        if (instance.isFromInception() || instance.isToNow()) {
            getJdbcTemplate().query(
                    "select min(rd.ts), max(rd.ts) " //
                    + "from reportInstancePoints rp "
                    + "  join reportInstanceData rd on rp.id=rd.reportInstancePointId "
                    + "where rp.reportInstanceId=?", new Object[]{instance.getId()},
                    new RowCallbackHandler() {

                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            if (instance.isFromInception()) {
                                instance.setReportStartTime(rs.getLong(1));
                            }
                            if (instance.isToNow()) {
                                instance.setReportEndTime(rs.getLong(2));
                            }
                        }
                    });
        }

        return count;
    }

    private Object[] appendParameters(Object[] toAppend, Object... params) {
        if (toAppend.length == 0) {
            return params;
        }
        if (params.length == 0) {
            return toAppend;
        }

        Object[] result = new Object[params.length + toAppend.length];
        System.arraycopy(params, 0, result, 0, params.length);
        System.arraycopy(toAppend, 0, result, params.length, toAppend.length);
        return result;
    }
    /**
     * This method guarantees that the data is provided to the setData handler method grouped by point (points are not
     * ordered), and sorted by time ascending.
     */
    private static final String REPORT_INSTANCE_POINT_SELECT = "select id, dataSourceName, pointName, dataType, " // 
            + "startValue, textRenderer, colour, consolidatedChart from reportInstancePoints ";
    private static final String REPORT_INSTANCE_DATA_SELECT = "select rd.pointValue, rda.textPointValueShort, " //
            + "  rda.textPointValueLong, rd.ts, rda.sourceValue "
            + "from reportInstanceData rd "
            + "  left join reportInstanceDataAnnotations rda on "
            + "      rd.pointValueId=rda.pointValueId and rd.reportInstancePointId=rda.reportInstancePointId ";

    public void reportInstanceData(int instanceId, final ReportDataStreamHandler handler) {
        // Retrieve point information.
        List<ReportPointInfo> pointInfos = getSimpleJdbcTemplate().query(REPORT_INSTANCE_POINT_SELECT + "where reportInstanceId=?",
                new ParameterizedRowMapper<ReportPointInfo>() {

                    @Override
                    public ReportPointInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ReportPointInfo rp = new ReportPointInfo();
                        rp.setReportPointId(rs.getInt(1));
                        rp.setDeviceName(rs.getString(2));
                        rp.setPointName(rs.getString(3));
                        rp.setMangoDataType(MangoDataType.fromMangoId(rs.getInt(4)));
                        String startValue = rs.getString(5);
                        if (startValue != null) {
                            rp.setStartValue(MangoValue.stringToValue(startValue, rp.getMangoDataType()));
                        }
                        rp.setTextRenderer((TextRenderer) SerializationHelper.readObject(rs.getBlob(6).getBinaryStream()));
                        rp.setColour(rs.getString(7));
                        rp.setConsolidatedChart(charToBool(rs.getString(8)));
                        return rp;
                    }
                }, instanceId);

        final ReportDataValue rdv = new ReportDataValue();
        for (final ReportPointInfo point : pointInfos) {
            handler.startPoint(point);

            rdv.setReportPointId(point.getReportPointId());
            getJdbcTemplate().query(REPORT_INSTANCE_DATA_SELECT + "where rd.reportInstancePointId=? order by rd.ts",
                    new Object[]{point.getReportPointId()}, new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    switch (point.getMangoDataType()) {
                        case NUMERIC:
                            rdv.setValue(new NumericValue(rs.getDouble(1)));
                            break;
                        case BINARY:
                            rdv.setValue(new BinaryValue(rs.getDouble(1) == 1));
                            break;
                        case MULTISTATE:
                            rdv.setValue(new MultistateValue(rs.getInt(1)));
                            break;
                        case ALPHANUMERIC:
                            rdv.setValue(new AlphanumericValue(rs.getString(2)));
                            if (rs.wasNull()) {
                                rdv.setValue(new AlphanumericValue(rs.getString(3)));
                            }
                            break;
                        case IMAGE:
                            rdv.setValue(new ImageValue(Integer.parseInt(rs.getString(2)), rs.getInt(1)));
                            break;
                        default:
                            rdv.setValue(null);
                    }

                    rdv.setTime(rs.getLong(4));
                    rdv.setAnnotation(rs.getString(5));

                    handler.pointData(rdv);
                }
            });
        }
        handler.done();
    }
    private static final String EVENT_SELECT = //
            "select eventId, typeId, typeRef1, typeRef2, activeTs, rtnApplicable, rtnTs, rtnCause, alarmLevel, message, " //
            + "ackTs, 0, ackUsername, alternateAckSource " //
            + "from reportInstanceEvents " //
            + "where reportInstanceId=? " //
            + "order by activeTs";
    private static final String EVENT_COMMENT_SELECT = "select username, typeKey, ts, commentText " //
            + "from reportInstanceUserComments " //
            + "where reportInstanceId=? and commentType=? " //
            + "order by ts";

    public List<EventInstance> getReportInstanceEvents(int instanceId) {
        // Get the events.
        final List<EventInstance> events = getSimpleJdbcTemplate().query(EVENT_SELECT, new EventDao.EventInstanceRowMapper(), instanceId);
        // Add in the comments.
        getJdbcTemplate().query(EVENT_COMMENT_SELECT, new Object[]{instanceId, /*TODO UserComment.CommentType.EVENT.name()*/}, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // Create the comment
                EventComment c = new EventComment();
                c.setUsername(rs.getString(1));
                c.setTs(rs.getDate(3));
                c.setComment(rs.getString(4));

                // Find the event and add the comment
                int eventId = rs.getInt(2);
                for (EventInstance event : events) {
                    if (event.getId() == eventId) {
                        if (event.getEventComments() == null) {
                            event.clearEventComments();
                        }
                        event.addEventComment(c);
                    }
                }
            }
        });
        // Done
        return events;
    }
    private static final String USER_COMMENT_SELECT = "select rc.username, rc.commentType, rc.typeKey, rp.pointName, " //
            + "  rc.ts, rc.commentText "
            + "from reportInstanceUserComments rc "
            + "  left join reportInstancePoints rp on rc.typeKey=rp.id and rc.commentType="
//TODO            + UserComment.CommentType.POINT.name()
            + " " + "where rc.reportInstanceId=? " + "order by rc.ts ";

    public List<ReportUserComment> getReportInstanceUserComments(int instanceId) {
        return getSimpleJdbcTemplate().query(USER_COMMENT_SELECT, new ReportCommentRowMapper(), instanceId);
    }

    class ReportCommentRowMapper implements ParameterizedRowMapper<ReportUserComment> {

        @Override
        public ReportUserComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportUserComment c = new ReportUserComment();
            c.setUsername(rs.getString(1));
//TODO            c.setCommentType(rs.getInt(2));
            c.setTypeKey(rs.getInt(3));
            c.setPointName(rs.getString(4));
            c.setTs(rs.getLong(5));
            c.setComment(rs.getString(6));
            return c;
        }
    }
}
