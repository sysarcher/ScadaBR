package com.serotonin.mango.db.dao;

import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.vo.event.AlarmLevel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.mango.Common;
import static com.serotonin.mango.db.dao.BaseDao.boolToChar;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.inject.Named;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

@Named
public class MaintenanceEventDao extends BaseDao {

    private static final String MAINTENANCE_EVENT_SELECT = //
            "select m.id, m.xid, m.dataSourceId, m.alias, m.alarmLevel, "
            + "  m.scheduleType, m.disabled, m.activeYear, m.activeMonth, m.activeDay, m.activeHour, m.activeMinute, "
            + "  m.activeSecond, m.activeCron, m.inactiveYear, m.inactiveMonth, m.inactiveDay, m.inactiveHour, "
            + "  m.inactiveMinute, m.inactiveSecond, m.inactiveCron, d.dataSourceType, d.name, d.xid " //
            + "from maintenanceEvents m join dataSources d on m.dataSourceId=d.id ";

    public MaintenanceEventDao() {
        super();
    }

    @Deprecated
    private MaintenanceEventDao(DataSource dataSource) {
        super(dataSource);
    }

    @Deprecated
    public static MaintenanceEventDao getInstance() {
        return new MaintenanceEventDao(Common.ctx.getDatabaseAccess().getDataSource());
    }

    public String generateUniqueXid() {
        return generateUniqueXid(MaintenanceEventVO.XID_PREFIX, "maintenanceEvents");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "maintenanceEvents");
    }

    public List<MaintenanceEventVO> getMaintenanceEvents() {
        return ejt.query(MAINTENANCE_EVENT_SELECT, new MaintenanceEventRowMapper());
    }

    public MaintenanceEventVO getMaintenanceEvent(int id) {
        MaintenanceEventVO me = ejt.queryForObject(MAINTENANCE_EVENT_SELECT + "where m.id=?", new MaintenanceEventRowMapper(), id);
        return me;
    }

    public MaintenanceEventVO getMaintenanceEvent(String xid) {
        try {
            return ejt.queryForObject(MAINTENANCE_EVENT_SELECT + "where m.xid=?", new MaintenanceEventRowMapper(), xid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    class MaintenanceEventRowMapper implements RowMapper<MaintenanceEventVO> {

        @Override
        public MaintenanceEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            MaintenanceEventVO me = new MaintenanceEventVO();
            int i = 0;
            me.setId(rs.getInt(++i));
            me.setXid(rs.getString(++i));
            me.setDataSourceId(rs.getInt(++i));
            me.setAlias(rs.getString(++i));
            me.setAlarmLevel(AlarmLevel.fromMangoDbId(rs.getInt(++i)));
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

    private void insertMaintenanceEvent(final MaintenanceEventVO me) {
        final int id = doInsert(new PreparedStatementCreator() {

            final static String SQL_INSERT = "insert into maintenanceEvents ("
                    + "  xid, dataSourceId, alias, alarmLevel, scheduleType, disabled, "
                    + "  activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, activeCron, "
                    + "  inactiveYear, inactiveMonth, inactiveDay, inactiveHour, inactiveMinute, inactiveSecond, inactiveCron "
                    + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, me.getXid());
                ps.setInt(2, me.getDataSourceId());
                ps.setString(3, me.getAlias());
                ps.setInt(4, me.getAlarmLevel().mangoDbId);
                ps.setInt(5, me.getScheduleType());
                ps.setString(6, boolToChar(me.isDisabled()));
                ps.setInt(7, me.getActiveYear());
                ps.setInt(8, me.getActiveMonth());
                ps.setInt(9, me.getActiveDay());
                ps.setInt(10, me.getActiveHour());
                ps.setInt(11, me.getActiveMinute());
                ps.setInt(12, me.getActiveSecond());
                ps.setString(13, me.getActiveCron());
                ps.setInt(14, me.getInactiveYear());
                ps.setInt(15, me.getInactiveMonth());
                ps.setInt(16, me.getInactiveDay());
                ps.setInt(17, me.getInactiveHour());
                ps.setInt(18, me.getInactiveMinute());
                ps.setInt(19, me.getInactiveSecond());
                ps.setString(20, me.getInactiveCron());
                return ps;
            }
        });
        me.setId(id);
        AuditEventType.raiseAddedEvent(AuditEventType.TYPE_MAINTENANCE_EVENT, me);
    }

    private void updateMaintenanceEvent(MaintenanceEventVO me) {
        MaintenanceEventVO old = getMaintenanceEvent(me.getId());
        ejt.update(
                "update maintenanceEvents set "
                + "  xid=?, dataSourceId=?, alias=?, alarmLevel=?, scheduleType=?, disabled=?, "
                + "  activeYear=?, activeMonth=?, activeDay=?, activeHour=?, activeMinute=?, activeSecond=?, activeCron=?, "
                + "  inactiveYear=?, inactiveMonth=?, inactiveDay=?, inactiveHour=?, inactiveMinute=?, inactiveSecond=?, "
                + "  inactiveCron=? "//
                + "where id=?",
                new Object[]{me.getXid(), me.getDataSourceId(), me.getAlias(), me.getAlarmLevel(),
                    me.getScheduleType(), boolToChar(me.isDisabled()), me.getActiveYear(), me.getActiveMonth(),
                    me.getActiveDay(), me.getActiveHour(), me.getActiveMinute(), me.getActiveSecond(),
                    me.getActiveCron(), me.getInactiveYear(), me.getInactiveMonth(), me.getInactiveDay(),
                    me.getInactiveHour(), me.getInactiveMinute(), me.getInactiveSecond(), me.getInactiveCron(),
                    me.getId()});
        AuditEventType.raiseChangedEvent(AuditEventType.TYPE_MAINTENANCE_EVENT, old, me);
    }

    public void deleteMaintenanceEventsForDataSource(int dataSourceId) {
        List<Integer> ids = ejt.queryForList("select id from maintenanceEvents where dataSourceId=?",
                new Object[]{dataSourceId}, Integer.class);
        for (Integer id : ids) {
            deleteMaintenanceEvent(id);
        }
    }

    public void deleteMaintenanceEvent(final int maintenanceEventId) {
        MaintenanceEventVO me = getMaintenanceEvent(maintenanceEventId);
        final JdbcTemplate ejt2 = ejt;
        if (me != null) {
            getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    ejt2.update("delete from eventHandlers where eventTypeId=" + EventSources.MAINTENANCE.mangoDbId
                            + " and eventTypeRef1=?", new Object[]{maintenanceEventId});
                    ejt2.update("delete from maintenanceEvents where id=?", new Object[]{maintenanceEventId});
                }
            });

            AuditEventType.raiseDeletedEvent(AuditEventType.TYPE_MAINTENANCE_EVENT, me);
        }
    }
}
