package com.serotonin.mango.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.support.TransactionTemplate;

public class MaintenanceEventDao extends BaseDao {

    private static final String MAINTENANCE_EVENT_SELECT = //
            "select m.id, m.xid, m.dataSourceId, m.alias, m.alarmLevel, "
            + "  m.scheduleType, m.disabled, m.activeYear, m.activeMonth, m.activeDay, m.activeHour, m.activeMinute, "
            + "  m.activeSecond, m.activeCron, m.inactiveYear, m.inactiveMonth, m.inactiveDay, m.inactiveHour, "
            + "  m.inactiveMinute, m.inactiveSecond, m.inactiveCron, d.dataSourceType, d.name, d.xid " //
            + "from maintenanceEvents m join dataSources d on m.dataSourceId=d.id ";

    public String generateUniqueXid() {
        return generateUniqueXid(MaintenanceEventVO.XID_PREFIX, "maintenanceEvents");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "maintenanceEvents");
    }

    public List<MaintenanceEventVO> getMaintenanceEvents() {
        return getSimpleJdbcTemplate().query(MAINTENANCE_EVENT_SELECT, new MaintenanceEventRowMapper());
    }

    public MaintenanceEventVO getMaintenanceEvent(int id) {
        MaintenanceEventVO me = getSimpleJdbcTemplate().queryForObject(MAINTENANCE_EVENT_SELECT + "where m.id=?", new MaintenanceEventRowMapper(), id);
        return me;
    }

    public MaintenanceEventVO getMaintenanceEvent(String xid) {
        return getSimpleJdbcTemplate().queryForObject(MAINTENANCE_EVENT_SELECT + "where m.xid=?", new MaintenanceEventRowMapper(), xid);
    }

    class MaintenanceEventRowMapper implements ParameterizedRowMapper<MaintenanceEventVO> {

        @Override
        public MaintenanceEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            MaintenanceEventVO me = new MaintenanceEventVO();
            int i = 0;
            me.setId(rs.getInt(++i));
            me.setXid(rs.getString(++i));
            me.setDataSourceId(rs.getInt(++i));
            me.setAlias(rs.getString(++i));
            me.setAlarmLevel(AlarmLevels.fromMangoId(rs.getInt(++i)));
            me.setScheduleType(rs.getInt(++i));
            me.setDisabled(charToBool(rs.getString(++i)));
            me.setActiveYear(rs.getInt(++i));
            me.setActiveMonth(rs.getInt(++i));
            me.setActiveDay(rs.getInt(++i));
            me.setActiveHour(rs.getInt(++i));
            me.setActiveMinute(rs.getInt(++i));
            me.setActiveSecond(rs.getInt(++i));
            me.setActiveCron(rs.getString(++i));
            me.setInactiveYear(rs.getInt(++i));
            me.setInactiveMonth(rs.getInt(++i));
            me.setInactiveDay(rs.getInt(++i));
            me.setInactiveHour(rs.getInt(++i));
            me.setInactiveMinute(rs.getInt(++i));
            me.setInactiveSecond(rs.getInt(++i));
            me.setInactiveCron(rs.getString(++i));
            me.setDataSourceTypeId(rs.getInt(++i));
            me.setDataSourceName(rs.getString(++i));
            me.setDataSourceXid(rs.getString(++i));
            return me;
        }
    }

    public void saveMaintenanceEvent(final MaintenanceEventVO me) {
        if (me.getId() == Common.NEW_ID) {
            insertMaintenanceEvent(me);
        } else {
            updateMaintenanceEvent(me);
        }
    }

    private void insertMaintenanceEvent(MaintenanceEventVO me) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("maintenanceEvents").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", me.getXid());
        params.put("dataSourceId", me.getDataSourceId());
        params.put("alias", me.getAlias());
        params.put("alarmLevel", me.getAlarmLevel().mangoId);
        params.put("scheduleType", me.getScheduleType());
        params.put("disabled", boolToChar(me.isDisabled()));
        params.put("activeYear", me.getActiveYear());
        params.put("activeMonth", me.getActiveMonth());
        params.put("activeDay", me.getActiveDay());
        params.put("activeHour", me.getActiveHour());
        params.put("activeMinute", me.getActiveMinute());
        params.put("activeSecond", me.getActiveSecond());
        params.put("activeCron", me.getActiveCron());
        params.put("inactiveYear", me.getInactiveYear());
        params.put("inactiveMonth", me.getInactiveMonth());
        params.put("inactiveDay", me.getInactiveDay());
        params.put("inactiveHour", me.getInactiveHour());
        params.put("inactiveMinute", me.getInactiveMinute());
        params.put("inactiveSecond", me.getInactiveSecond());
        params.put("inactiveCron", me.getInactiveCron());

        Number id = insertActor.executeAndReturnKey(params);
        me.setId(id.intValue());

        AuditEventType.raiseAddedEvent(AuditEventType.TYPE_MAINTENANCE_EVENT, me);
    }

    private void updateMaintenanceEvent(MaintenanceEventVO me) {
        MaintenanceEventVO old = getMaintenanceEvent(me.getId());
        getSimpleJdbcTemplate().update(
                "update maintenanceEvents set "
                + "  xid=?, dataSourceId=?, alias=?, alarmLevel=?, scheduleType=?, disabled=?, "
                + "  activeYear=?, activeMonth=?, activeDay=?, activeHour=?, activeMinute=?, activeSecond=?, activeCron=?, "
                + "  inactiveYear=?, inactiveMonth=?, inactiveDay=?, inactiveHour=?, inactiveMinute=?, inactiveSecond=?, "
                + "  inactiveCron=? "//
                + "where id=?",
                me.getXid(), me.getDataSourceId(), me.getAlias(), me.getAlarmLevel().mangoId,
                me.getScheduleType(), boolToChar(me.isDisabled()), me.getActiveYear(), me.getActiveMonth(),
                me.getActiveDay(), me.getActiveHour(), me.getActiveMinute(), me.getActiveSecond(),
                me.getActiveCron(), me.getInactiveYear(), me.getInactiveMonth(), me.getInactiveDay(),
                me.getInactiveHour(), me.getInactiveMinute(), me.getInactiveSecond(), me.getInactiveCron(),
                me.getId());
        AuditEventType.raiseChangedEvent(AuditEventType.TYPE_MAINTENANCE_EVENT, old, me);
    }

    public void deleteMaintenanceEventsForDataSource(int dataSourceId) {
        List<Integer> ids = getJdbcTemplate().queryForList("select id from maintenanceEvents where dataSourceId=?",
                new Object[]{dataSourceId}, Integer.class);
        for (Integer id : ids) {
            deleteMaintenanceEvent(id);
        }
    }

    public void deleteMaintenanceEvent(final int maintenanceEventId) {
        MaintenanceEventVO me = getMaintenanceEvent(maintenanceEventId);
        if (me != null) {
            new TransactionTemplate(getTransactionManager()).execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    getSimpleJdbcTemplate().update("delete from eventHandlers where eventTypeId=" + EventType.EventSources.MAINTENANCE
                            + " and eventTypeRef1=?", maintenanceEventId);
                    getSimpleJdbcTemplate().update("delete from maintenanceEvents where id=?", maintenanceEventId);
                }
            });

            AuditEventType.raiseDeletedEvent(AuditEventType.TYPE_MAINTENANCE_EVENT, me);
        }
    }
}
