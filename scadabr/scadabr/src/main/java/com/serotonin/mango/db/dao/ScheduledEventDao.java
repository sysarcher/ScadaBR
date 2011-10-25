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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.event.ScheduledEventVO;

/**
 * @author Matthew Lohbihler
 *
 */
@Service
public class ScheduledEventDao extends BaseDao {

    private static final String SCHEDULED_EVENT_SELECT = "select id, xid, alias, alarmLevel, scheduleType, "
            + "  returnToNormal, disabled, activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, "
            + "  activeCron, inactiveYear, inactiveMonth, inactiveDay, inactiveHour, inactiveMinute, inactiveSecond, "
            + "inactiveCron from scheduledEvents ";
    @Autowired
    private EventManager eventManager;

    public String generateUniqueXid() {
        return generateUniqueXid(ScheduledEventVO.XID_PREFIX, "scheduledEvents");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "scheduledEvents");
    }

    public List<ScheduledEventVO> getScheduledEvents() {
        return getSimpleJdbcTemplate().query(SCHEDULED_EVENT_SELECT + " order by scheduleType", new ScheduledEventRowMapper());
    }

    public ScheduledEventVO getScheduledEvent(int id) {
        return getSimpleJdbcTemplate().queryForObject(SCHEDULED_EVENT_SELECT + "where id=?", new ScheduledEventRowMapper(), id);
    }

    public ScheduledEventVO getScheduledEvent(String xid) {
        return getSimpleJdbcTemplate().queryForObject(SCHEDULED_EVENT_SELECT + "where xid=?", new ScheduledEventRowMapper(), xid);
    }

    class ScheduledEventRowMapper implements ParameterizedRowMapper<ScheduledEventVO> {

        @Override
        public ScheduledEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScheduledEventVO se = new ScheduledEventVO();
            int i = 0;
            se.setId(rs.getInt(++i));
            se.setXid(rs.getString(++i));
            se.setAlias(rs.getString(++i));
            se.setAlarmLevel(AlarmLevels.fromMangoId(rs.getInt(++i)));
            se.setScheduleType(rs.getInt(++i));
            se.setReturnToNormal(charToBool(rs.getString(++i)));
            se.setDisabled(charToBool(rs.getString(++i)));
            se.setActiveYear(rs.getInt(++i));
            se.setActiveMonth(rs.getInt(++i));
            se.setActiveDay(rs.getInt(++i));
            se.setActiveHour(rs.getInt(++i));
            se.setActiveMinute(rs.getInt(++i));
            se.setActiveSecond(rs.getInt(++i));
            se.setActiveCron(rs.getString(++i));
            se.setInactiveYear(rs.getInt(++i));
            se.setInactiveMonth(rs.getInt(++i));
            se.setInactiveDay(rs.getInt(++i));
            se.setInactiveHour(rs.getInt(++i));
            se.setInactiveMinute(rs.getInt(++i));
            se.setInactiveSecond(rs.getInt(++i));
            se.setInactiveCron(rs.getString(++i));
            return se;
        }
    }

    public void saveScheduledEvent(final ScheduledEventVO se) {
        if (se.getId() == Common.NEW_ID) {
            insertScheduledEvent(se);
        } else {
            updateScheduledEvent(se);
        }
    }

    private void insertScheduledEvent(ScheduledEventVO se) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("pointHierarchy").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", se.getXid());
        params.put("alarmLevel", se.getAlarmLevel().mangoId);
        params.put("alias", se.getAlias());
        params.put("scheduleType", se.getScheduleType());
        params.put("returnToNormal", boolToChar(se.isReturnToNormal()));
        params.put("disabled", boolToChar(se.isDisabled()));
        params.put("activeYear", se.getActiveYear());
        params.put("activeMonth", se.getActiveMonth());
        params.put("activeDay", se.getActiveDay());
        params.put("activeHour", se.getActiveHour());
        params.put("activeMinute", se.getActiveMinute());
        params.put("activeSecond", se.getActiveSecond());
        params.put("activeCron", se.getActiveCron());
        params.put("inactiveYear", se.getInactiveYear());
        params.put("inactiveMonth", se.getInactiveMonth());
        params.put("inactiveDay", se.getInactiveDay());
        params.put("inactiveHour", se.getInactiveHour());
        params.put("inactiveMinute", se.getInactiveMinute());
        params.put("inactiveSecond", se.getInactiveSecond());
        params.put("inactiveCron", se.getInactiveCron());

        Number id = insertActor.executeAndReturnKey(params);
        se.setId(id.intValue());

        eventManager.raiseAddedEvent(AuditEventType.TYPE_SCHEDULED_EVENT, se);
    }

    private void updateScheduledEvent(ScheduledEventVO se) {
        ScheduledEventVO old = getScheduledEvent(se.getId());
        getSimpleJdbcTemplate().update(
                "update scheduledEvents set "
                + "  xid=?, alarmLevel=?, alias=?, scheduleType=?, returnToNormal=?, disabled=?, "
                + "  activeYear=?, activeMonth=?, activeDay=?, activeHour=?, activeMinute=?, activeSecond=?, activeCron=?, "
                + "  inactiveYear=?, inactiveMonth=?, inactiveDay=?, inactiveHour=?, inactiveMinute=?, inactiveSecond=?, "
                + "  inactiveCron=? " + "where id=?", se.getXid(), se.getAlarmLevel().mangoId,
                se.getAlias(), se.getScheduleType(), boolToChar(se.isReturnToNormal()),
                boolToChar(se.isDisabled()), se.getActiveYear(), se.getActiveMonth(),
                se.getActiveDay(), se.getActiveHour(), se.getActiveMinute(), se.getActiveSecond(),
                se.getActiveCron(), se.getInactiveYear(), se.getInactiveMonth(), se.getInactiveDay(),
                se.getInactiveHour(), se.getInactiveMinute(), se.getInactiveSecond(),
                se.getInactiveCron(), se.getId());
        eventManager.raiseChangedEvent(AuditEventType.TYPE_SCHEDULED_EVENT, old, se);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteScheduledEvent(final int scheduledEventId) {
        ScheduledEventVO se = getScheduledEvent(scheduledEventId);
        if (se != null) {
            getSimpleJdbcTemplate().update("delete from eventHandlers where eventTypeId=" + EventType.EventSources.SCHEDULED
                    + " and eventTypeRef1=?", scheduledEventId);
            getSimpleJdbcTemplate().update("delete from scheduledEvents where id=?", scheduledEventId);

            eventManager.raiseDeletedEvent(AuditEventType.TYPE_SCHEDULED_EVENT, se);
        }
    }
}
