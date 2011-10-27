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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.CompoundDetectorEventType;
import com.serotonin.mango.rt.event.type.DataPointEventType;
import com.serotonin.mango.rt.event.type.DataSourceEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.MaintenanceEventType;
import com.serotonin.mango.rt.event.type.PublisherEventType;
import com.serotonin.mango.rt.event.type.ScheduledEventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.mango.vo.event.EventHandlerVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.mango.web.dwr.EventsDwr;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.web.i18n.LocalizableMessageParseException;

@Service
public class EventDao extends BaseDao {

    private static final int MAX_PENDING_EVENTS = 100;
    @Autowired
    private UserCommentDao userCommentDao;
    @Autowired
    private EventManager eventManager;

    public void saveEvent(EventInstance event) {
        if (event.getId() == Common.NEW_ID) {
            insertEvent(event);
        } else {
            updateEvent(event);
        }
    }

    private void insertEvent(EventInstance event) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("events").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap();

        EventType type = event.getEventType();

        params.put("typeId", type.getEventSourceId());
        params.put("typeRef1", type.getReferenceId1());
        params.put("typeRef2", type.getReferenceId2());
        params.put("activeTs", event.getActiveTimestamp());
        params.put("rtnApplicable", boolToChar(event.isRtnApplicable()));
        if (!event.isActive()) {
            params.put("rtnTs ", event.getRtnTimestamp());
            params.put("rtnCause", event.getRtnCause());
        }
        params.put("alarmLevel", event.getAlarmLevel().mangoId);
        params.put("message", event.getMessage().serialize());
        if (!event.isAlarm()) {
            event.setAcknowledgedTimestamp(event.getActiveTimestamp());
            params.put("ackTs", event.getAcknowledgedTimestamp());
        }

        Number id = insertActor.executeAndReturnKey(params);
        event.setId(id.intValue());

        event.clearEventComments();
    }
    private static final String EVENT_UPDATE = "update events set rtnTs=?, rtnCause=? where id=?";

    private void updateEvent(EventInstance event) {
        getSimpleJdbcTemplate().update(EVENT_UPDATE,
                event.getRtnTimestamp(), event.getRtnCause(), event.getId());
        updateCache(event);
    }
    private static final String EVENT_ACK = "update events set ackTs=?, ackUserId=?, alternateAckSource=? where id=? and ackTs is null";
    private static final String USER_EVENT_ACK = "update userEvents set silenced=? where eventId=?";

    public void ackEvent(int eventId, long time, int userId,
            int alternateAckSource) {
        // Ack the event
        getSimpleJdbcTemplate().update(EVENT_ACK, time, userId == 0 ? null : userId, alternateAckSource, eventId);
        // Silence the user events
        getSimpleJdbcTemplate().update(USER_EVENT_ACK, boolToChar(true), eventId);
        // Clear the cache
        clearCache();
    }
    private static final String USER_EVENTS_INSERT = "insert into userEvents (eventId, userId, silenced) values (?,?,?)";

    public void insertUserEvents(final int eventId,
            final List<Integer> userIds, final boolean alarm) {
        getJdbcTemplate().batchUpdate(USER_EVENTS_INSERT, new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return userIds.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setInt(1, eventId);
                ps.setInt(2, userIds.get(i));
                ps.setString(3, boolToChar(!alarm));
            }
        });

        if (alarm) {
            for (int userId : userIds) {
                removeUserIdFromCache(userId);
            }
        }
    }
    private static final String BASIC_EVENT_SELECT = "select e.id, e.typeId, e.typeRef1, e.typeRef2, e.activeTs, e.rtnApplicable, e.rtnTs, e.rtnCause, "
            + "  e.alarmLevel, e.message, e.ackTs, e.ackUserId, u.username, e.alternateAckSource "
            + "from events e " + "  left join users u on e.ackUserId=u.id ";

    public List<EventInstance> getActiveEvents() {
        List<EventInstance> results = getSimpleJdbcTemplate().query(BASIC_EVENT_SELECT
                + "where e.rtnApplicable=? and e.rtnTs is null", new EventInstanceRowMapper(), boolToChar(true));
        userCommentDao.populateComments(results);
        return results;
    }
    private static final String EVENT_SELECT_WITH_USER_DATA = "select e.id, e.typeId, e.typeRef1, e.typeRef2, e.activeTs, e.rtnApplicable, e.rtnTs, e.rtnCause, "
            + "  e.alarmLevel, e.message, e.ackTs, e.ackUserId, u.username, e.alternateAckSource, ue.silenced "
            + "from events e "
            + "  left join users u on e.ackUserId=u.id "
            + "  left join userEvents ue on e.id=ue.eventId ";

    public List<EventInstance> getEventsForDataPoint(int dataPointId, int userId) {
        List<EventInstance> results = getSimpleJdbcTemplate().query(EVENT_SELECT_WITH_USER_DATA
                + "where e.typeId=" + EventType.EventSources.DATA_POINT
                + "  and e.typeRef1=? " + "  and ue.userId=? "
                + "order by e.activeTs desc", new UserEventInstanceRowMapper(), dataPointId, userId);
        userCommentDao.populateComments(results);
        return results;
    }

    public List<EventInstance> getPendingEventsForDataPoint(int dataPointId,
            int userId) {
        // Check the cache
        List<EventInstance> userEvents = getFromCache(userId);
        if (userEvents == null) {
            // This is a potentially long running query, so run it offline.
            userEvents = Collections.emptyList();
            addToCache(userId, userEvents);
            Common.timer.execute(new UserPendingEventRetriever(userId));
        }

        List<EventInstance> list = null;
        for (EventInstance e : userEvents) {
            if (e.getEventType().getDataPointId() == dataPointId) {
                if (list == null) {
                    list = new ArrayList<EventInstance>();
                }
                list.add(e);
            }
        }

        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    class UserPendingEventRetriever implements Runnable {

        private final int userId;

        UserPendingEventRetriever(int userId) {
            this.userId = userId;
        }

        @Override
        public void run() {
            addToCache(
                    userId,
                    getPendingEvents(EventType.EventSources.DATA_POINT, -1,
                    userId));
        }
    }

    public List<EventInstance> getPendingEventsForDataSource(int dataSourceId,
            int userId) {
        return getPendingEvents(EventType.EventSources.DATA_SOURCE,
                dataSourceId, userId);
    }

    public List<EventInstance> getPendingEventsForPublisher(int publisherId,
            int userId) {
        return getPendingEvents(EventType.EventSources.PUBLISHER, publisherId,
                userId);
    }

    List<EventInstance> getPendingEvents(int typeId, int typeRef1, int userId) {
        Object[] params;
        StringBuilder sb = new StringBuilder();
        sb.append(EVENT_SELECT_WITH_USER_DATA);
        sb.append("where e.typeId=?");

        if (typeRef1 == -1) {
            params = new Object[]{typeId, userId, boolToChar(true)};
        } else {
            sb.append("  and e.typeRef1=?");
            params = new Object[]{typeId, typeRef1, userId, boolToChar(true)};
        }
        sb.append("  and ue.userId=? ");
        sb.append("  and (e.ackTs is null or (e.rtnApplicable=? and e.rtnTs is null and e.alarmLevel > 0)) ");
        sb.append("order by e.activeTs desc");

        List<EventInstance> results = getJdbcTemplate().query(sb.toString(), new UserEventInstanceRowMapper(), params);
        userCommentDao.populateComments(results);
        return results;
    }

    public List<EventInstance> getPendingEvents(int userId) {
        List<EventInstance> results = getLimitJdbcTemplate(MAX_PENDING_EVENTS).query(
                EVENT_SELECT_WITH_USER_DATA
                + "where ue.userId=? and e.ackTs is null order by e.activeTs desc",
                new UserEventInstanceRowMapper(), userId);
        userCommentDao.populateComments(results);
        return results;
    }

    private EventInstance getEventInstance(int eventId) {
        return getJdbcTemplate().queryForObject(BASIC_EVENT_SELECT + "where e.id=?",
                new EventInstanceRowMapper(), eventId);
    }

    public static class EventInstanceRowMapper implements
            ParameterizedRowMapper<EventInstance> {

        @Override
        public EventInstance mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            EventType type = createEventType(rs, 2);

            LocalizableMessage message;
            try {
                message = LocalizableMessage.deserialize(rs.getString(10));
            } catch (LocalizableMessageParseException e) {
                message = new LocalizableMessage("common.default",
                        rs.getString(10));
            }

            EventInstance event = new EventInstance(type, rs.getLong(5),
                    charToBool(rs.getString(6)), AlarmLevels.fromMangoId(rs.getInt(9)), message, null);
            event.setId(rs.getInt(1));
            long rtnTs = rs.getLong(7);
            if (!rs.wasNull()) {
                event.returnToNormal(rtnTs, rs.getInt(8));
            }
            long ackTs = rs.getLong(11);
            if (!rs.wasNull()) {
                event.setAcknowledgedTimestamp(ackTs);
                event.setAcknowledgedByUserId(rs.getInt(12));
                if (!rs.wasNull()) {
                    event.setAcknowledgedByUsername(rs.getString(13));
                }
                event.setAlternateAckSource(rs.getInt(14));
            }

            return event;
        }
    }

    class UserEventInstanceRowMapper extends EventInstanceRowMapper {

        @Override
        public EventInstance mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            EventInstance event = super.mapRow(rs, rowNum);
            event.setSilenced(charToBool(rs.getString(15)));
            if (!rs.wasNull()) {
                event.setUserNotified(true);
            }
            return event;
        }
    }

    static EventType createEventType(ResultSet rs, int offset)
            throws SQLException {
        final int typeId = rs.getInt(offset);
        switch (typeId) {
            case EventType.EventSources.DATA_POINT:
                return new DataPointEventType(rs.getInt(offset + 1), rs.getInt(offset + 2));
            case EventType.EventSources.DATA_SOURCE:
                return new DataSourceEventType(rs.getInt(offset + 1), rs.getInt(offset + 2));
            case EventType.EventSources.SYSTEM:
                return new SystemEventType(rs.getInt(offset + 1), rs.getInt(offset + 2));
            case EventType.EventSources.COMPOUND:
                return new CompoundDetectorEventType(rs.getInt(offset + 1));
            case EventType.EventSources.SCHEDULED:
                return new ScheduledEventType(rs.getInt(offset + 1));
            case EventType.EventSources.PUBLISHER:
                return new PublisherEventType(rs.getInt(offset + 1), rs.getInt(offset + 2));
            case EventType.EventSources.AUDIT:
                return new AuditEventType(rs.getInt(offset + 1), rs.getInt(offset + 2));
            case EventType.EventSources.MAINTENANCE:
                return new MaintenanceEventType(rs.getInt(offset + 1));
            default:
                throw new ShouldNeverHappenException("Unknown event type: "
                        + typeId);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public int purgeEventsBefore(final long time) {
        // Find a list of event ids with no remaining acknowledgements pending.
        int count = getSimpleJdbcTemplate().update("delete from events "
                + "where activeTs<? "
                + "  and ackTs is not null "
                + "  and (rtnApplicable=? or (rtnApplicable=? and rtnTs is not null))",
                time, boolToChar(false), boolToChar(true));

        clearCache();

        return count;
    }

    public int getEventCount() {
        return getSimpleJdbcTemplate().queryForInt("select count(*) from events");
    }

    public List<EventInstance> search(int eventId, int eventSourceType,
            String status, AlarmLevels alarmLevel, final String[] keywords, int userId,
            final ResourceBundle bundle, final int from, final int to,
            final Date date) {
        return search(eventId, eventSourceType, status, alarmLevel, keywords,
                null, null, userId, bundle, from, to, date);
    }

    public List<EventInstance> search(int eventId, int eventSourceType,
            String status, AlarmLevels alarmLevel, final String[] keywords,
            Date dateFrom, Date dateTo, int userId,
            final ResourceBundle bundle, final int from, final int to,
            final Date date) {
        List<String> where = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();

        StringBuilder sql = new StringBuilder();
        sql.append(EVENT_SELECT_WITH_USER_DATA);
        sql.append("where ue.userId=?");
        params.add(userId);

        if (eventId != 0) {
            where.add("e.id=?");
            params.add(eventId);
        }

        if (eventSourceType != -1) {
            where.add("e.typeId=?");
            params.add(eventSourceType);
        }

        if (EventsDwr.STATUS_ACTIVE.equals(status)) {
            where.add("e.rtnApplicable=? and e.rtnTs is null");
            params.add(boolToChar(true));
        } else if (EventsDwr.STATUS_RTN.equals(status)) {
            where.add("e.rtnApplicable=? and e.rtnTs is not null");
            params.add(boolToChar(true));
        } else if (EventsDwr.STATUS_NORTN.equals(status)) {
            where.add("e.rtnApplicable=?");
            params.add(boolToChar(false));
        }

        if (alarmLevel != null) {
            where.add("e.alarmLevel=?");
            params.add(alarmLevel.mangoId);
        }

        if (dateFrom != null) {
            where.add("activeTs>=?");
            params.add(dateFrom.getTime());
        }

        if (dateTo != null) {
            where.add("activeTs<?");
            params.add(dateTo.getTime());
        }

        if (!where.isEmpty()) {
            for (String s : where) {
                sql.append(" and ");
                sql.append(s);
            }
        }
        sql.append(" order by e.activeTs desc");

        final List<EventInstance> results = new ArrayList<EventInstance>();
        final UserEventInstanceRowMapper rowMapper = new UserEventInstanceRowMapper();

        final int[] data = new int[2];

        getJdbcTemplate().query(sql.toString(), params.toArray(), new ResultSetExtractor() {

            @Override
            public Object extractData(ResultSet rs) throws SQLException,
                    DataAccessException {
                int row = 0;
                long dateTs = date == null ? -1 : date.getTime();
                int startRow = -1;

                while (rs.next()) {
                    EventInstance e = rowMapper.mapRow(rs, 0);
                    userCommentDao.populateComments(e);
                    boolean add = true;

                    if (keywords != null) {
                        // Do the text search. If the instance has a match, put
                        // it in the result. Otherwise ignore.
                        StringBuilder text = new StringBuilder();
                        text.append(e.getMessage().getLocalizedMessage(bundle));
                        for (UserComment comment : e.getEventComments()) {
                            text.append(' ').append(comment.getComment());
                        }

                        String[] values = text.toString().split("\\s+");

                        for (String keyword : keywords) {
                            if (keyword.startsWith("-")) {
                                if (StringUtils.globWhiteListMatchIgnoreCase(
                                        values, keyword.substring(1))) {
                                    add = false;
                                    break;
                                }
                            } else {
                                if (!StringUtils.globWhiteListMatchIgnoreCase(
                                        values, keyword)) {
                                    add = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (add) {
                        if (date != null) {
                            if (e.getActiveTimestamp() <= dateTs
                                    && results.size() < to - from) {
                                if (startRow == -1) {
                                    startRow = row;
                                }
                                results.add(e);
                            }
                        } else if (row >= from && row < to) {
                            results.add(e);
                        }

                        row++;
                    }
                }

                data[0] = row;
                data[1] = startRow;

                return null;
            }
        });

        searchRowCount = data[0];
        startRow = data[1];

        return results;
    }
    private int searchRowCount;
    private int startRow;

    public int getSearchRowCount() {
        return searchRowCount;
    }

    public int getStartRow() {
        return startRow;
    }

    //
    // /
    // / Event handlers
    // /
    //
    public String generateUniqueXid() {
        return generateUniqueXid(EventHandlerVO.XID_PREFIX, "eventHandlers");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "eventHandlers");
    }

    public EventType getEventHandlerType(int handlerId) {
        return getSimpleJdbcTemplate().queryForObject(
                "select eventTypeId, eventTypeRef1, eventTypeRef2 from eventHandlers where id=?",
                new ParameterizedRowMapper<EventType>() {

                    @Override
                    public EventType mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return createEventType(rs, 1);
                    }
                }, handlerId);
    }

    public List<EventHandlerVO> getEventHandlers(EventType type) {
        return getEventHandlers(type.getEventSourceId(),
                type.getReferenceId1(), type.getReferenceId2());
    }

    public List<EventHandlerVO> getEventHandlers(EventTypeVO type) {
        return getEventHandlers(type.getTypeId(), type.getTypeRef1(),
                type.getTypeRef2());
    }

    public List<EventHandlerVO> getEventHandlers() {
        return getSimpleJdbcTemplate().query(EVENT_HANDLER_SELECT, new EventHandlerRowMapper());
    }

    /**
     * Note: eventHandlers.eventTypeRef2 matches on both the given ref2 and 0.
     * This is to allow a single set of event handlers to be defined for user
     * login events, rather than have to individually define them for each user.
     */
    private List<EventHandlerVO> getEventHandlers(int typeId, int ref1, int ref2) {
        return getSimpleJdbcTemplate().query(EVENT_HANDLER_SELECT
                + "where eventTypeId=? and eventTypeRef1=? "
                + "  and (eventTypeRef2=? or eventTypeRef2=0)",
                new EventHandlerRowMapper(), typeId, ref1, ref2);
    }

    public EventHandlerVO getEventHandler(int eventHandlerId) {
        return getSimpleJdbcTemplate().queryForObject(EVENT_HANDLER_SELECT + "where id=?",
                new EventHandlerRowMapper(), eventHandlerId);
    }

    public EventHandlerVO getEventHandler(String xid) {
        return getSimpleJdbcTemplate().queryForObject(EVENT_HANDLER_SELECT + "where xid=?",
                new EventHandlerRowMapper(), xid);
    }
    private static final String EVENT_HANDLER_SELECT = "select id, xid, alias, data from eventHandlers ";

    class EventHandlerRowMapper implements ParameterizedRowMapper<EventHandlerVO> {

        @Override
        public EventHandlerVO mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            EventHandlerVO h = (EventHandlerVO) SerializationHelper.readObject(rs.getBlob(4).getBinaryStream());
            h.setId(rs.getInt(1));
            h.setXid(rs.getString(2));
            h.setAlias(rs.getString(3));
            return h;
        }
    }

    public EventHandlerVO saveEventHandler(final EventType type,
            final EventHandlerVO handler) {
        if (type == null) {
            return saveEventHandler(0, 0, 0, handler);
        }
        return saveEventHandler(type.getEventSourceId(),
                type.getReferenceId1(), type.getReferenceId2(), handler);
    }

    public EventHandlerVO saveEventHandler(final EventTypeVO type,
            final EventHandlerVO handler) {
        if (type == null) {
            return saveEventHandler(0, 0, 0, handler);
        }
        return saveEventHandler(type.getTypeId(), type.getTypeRef1(),
                type.getTypeRef2(), handler);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    private EventHandlerVO saveEventHandler(final int typeId,
            final int typeRef1, final int typeRef2, final EventHandlerVO handler) {
        if (handler.getId() == Common.NEW_ID) {
            insertEventHandler(typeId, typeRef1, typeRef2,
                    handler);
        } else {
            updateEventHandler(handler);
        }
        return getEventHandler(handler.getId());
    }

    void insertEventHandler(int typeId, int typeRef1, int typeRef2,
            EventHandlerVO handler) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("eventHandlers").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap();
        params.put("xid", handler.getXid());
        params.put("alias", handler.getAlias());
        params.put("eventTypeId", typeId);
        params.put("eventTypeRef1", typeRef1);
        params.put("eventTypeRef2", typeRef2);
        params.put("data", SerializationHelper.writeObjectToArray(handler));

        Number id = insertActor.executeAndReturnKey(params);
        handler.setId(id.intValue());

        eventManager.raiseAddedEvent(AuditEventType.TYPE_EVENT_HANDLER,
                handler);
    }

    void updateEventHandler(EventHandlerVO handler) {
        EventHandlerVO old = getEventHandler(handler.getId());
        getSimpleJdbcTemplate().update(
                "update eventHandlers set xid=?, alias=?, data=? where id=?",
                handler.getXid(), handler.getAlias(), SerializationHelper.writeObjectToArray(handler), handler.getId());

        eventManager.raiseChangedEvent(AuditEventType.TYPE_EVENT_HANDLER,
                old, handler);
    }

    public void deleteEventHandler(final int handlerId) {
        EventHandlerVO handler = getEventHandler(handlerId);
        getSimpleJdbcTemplate().update("delete from eventHandlers where id=?", handlerId);
        eventManager.raiseDeletedEvent(AuditEventType.TYPE_EVENT_HANDLER,
                handler);
    }
    //
    // /
    // / User alarms
    // /
    //
    private static final String SILENCED_SELECT = "select ue.silenced "
            + "from events e " + "  join userEvents ue on e.id=ue.eventId "
            + "where e.id=? " + "  and ue.userId=? " + "  and e.ackTs is null";

    public boolean toggleSilence(int eventId, int userId) {
        String result = getSimpleJdbcTemplate().queryForObject(SILENCED_SELECT, String.class, eventId, userId);
        if (result == null) {
            return true;
        }

        boolean silenced = !charToBool(result);
        getSimpleJdbcTemplate().update(
                "update userEvents set silenced=? where eventId=? and userId=?",
                boolToChar(silenced), eventId, userId);
        return silenced;
    }

    public AlarmLevels getHighestUnsilencedAlarmLevel(int userId) {
        return AlarmLevels.fromMangoId(getSimpleJdbcTemplate().queryForInt("select max(e.alarmLevel) from userEvents u "
                + "  join events e on u.eventId=e.id "
                + "where u.silenced=? and u.userId=?",
                boolToChar(false), userId));
    }

    //
    // /
    // / Pending event caching
    // /
    //
    static class PendingEventCacheEntry {

        private final List<EventInstance> list;
        private final long createTime;

        public PendingEventCacheEntry(List<EventInstance> list) {
            this.list = list;
            createTime = System.currentTimeMillis();
        }

        public List<EventInstance> getList() {
            return list;
        }

        public boolean hasExpired() {
            return System.currentTimeMillis() - createTime > CACHE_TTL;
        }
    }
    private static Map<Integer, PendingEventCacheEntry> pendingEventCache = new ConcurrentHashMap<Integer, PendingEventCacheEntry>();
    private static final long CACHE_TTL = 300000; // 5 minutes

    public static List<EventInstance> getFromCache(int userId) {
        PendingEventCacheEntry entry = pendingEventCache.get(userId);
        if (entry == null) {
            return null;
        }
        if (entry.hasExpired()) {
            pendingEventCache.remove(userId);
            return null;
        }
        return entry.getList();
    }

    public static void addToCache(int userId, List<EventInstance> list) {
        pendingEventCache.put(userId, new PendingEventCacheEntry(list));
    }

    public static void updateCache(EventInstance event) {
        if (event.isAlarm()
                && event.getEventType().getEventSourceId() == EventType.EventSources.DATA_POINT) {
            pendingEventCache.clear();
        }
    }

    public static void removeUserIdFromCache(int userId) {
        pendingEventCache.remove(userId);
    }

    public static void clearCache() {
        pendingEventCache.clear();
    }
}
