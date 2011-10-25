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
import com.serotonin.mango.vo.event.CompoundEventDetectorVO;

/**
 * @author Matthew Lohbihler
 */
@Service
public class CompoundEventDetectorDao extends BaseDao {

    private static final String COMPOUND_EVENT_DETECTOR_SELECT = "select id, xid, name, alarmLevel, returnToNormal, disabled, conditionText from compoundEventDetectors ";
    @Autowired
    private EventManager eventManager;

    public String generateUniqueXid() {
        return generateUniqueXid(CompoundEventDetectorVO.XID_PREFIX, "compoundEventDetectors");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "compoundEventDetectors");
    }

    public List<CompoundEventDetectorVO> getCompoundEventDetectors() {
        return getSimpleJdbcTemplate().query(COMPOUND_EVENT_DETECTOR_SELECT + "order by name", new CompoundEventDetectorRowMapper());
    }

    public CompoundEventDetectorVO getCompoundEventDetector(int id) {
        return getSimpleJdbcTemplate().queryForObject(COMPOUND_EVENT_DETECTOR_SELECT + "where id=?", new CompoundEventDetectorRowMapper(), id);
    }

    public CompoundEventDetectorVO getCompoundEventDetector(String xid) {
        return getSimpleJdbcTemplate().queryForObject(COMPOUND_EVENT_DETECTOR_SELECT + "where xid=?", new CompoundEventDetectorRowMapper(), xid);
    }

    class CompoundEventDetectorRowMapper implements ParameterizedRowMapper<CompoundEventDetectorVO> {

        @Override
        public CompoundEventDetectorVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            CompoundEventDetectorVO ced = new CompoundEventDetectorVO();
            int i = 0;
            ced.setId(rs.getInt(++i));
            ced.setXid(rs.getString(++i));
            ced.setName(rs.getString(++i));
            ced.setAlarmLevel(AlarmLevels.fromMangoId(rs.getInt(++i)));
            ced.setReturnToNormal(charToBool(rs.getString(++i)));
            ced.setDisabled(charToBool(rs.getString(++i)));
            ced.setCondition(rs.getString(++i));
            return ced;
        }
    }

    public void saveCompoundEventDetector(final CompoundEventDetectorVO ced) {
        if (ced.getId() == Common.NEW_ID) {
            insertCompoundEventDetector(ced);
        } else {
            updateCompoundEventDetector(ced);
        }
    }

    private void insertCompoundEventDetector(CompoundEventDetectorVO ced) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("compoundEventDetectors").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", ced.getXid());
        params.put("name", ced.getName());
        params.put("alarmLevel", ced.getAlarmLevel().mangoId);
        params.put("returnToNormal", boolToChar(ced.isReturnToNormal()));
        params.put("disabled", boolToChar(ced.isDisabled()));
        params.put("conditionText", ced.getCondition());

        Number id = insertActor.executeAndReturnKey(params);
        ced.setId(id.intValue());

        eventManager.raiseAddedEvent(AuditEventType.TYPE_COMPOUND_EVENT_DETECTOR, ced);
    }
    private static final String COMPOUND_EVENT_DETECTOR_UPDATE = "update compoundEventDetectors set xid=?, name=?, alarmLevel=?, returnToNormal=?, disabled=?, conditionText=? "
            + "where id=?";

    private void updateCompoundEventDetector(CompoundEventDetectorVO ced) {
        CompoundEventDetectorVO old = getCompoundEventDetector(ced.getId());

        getSimpleJdbcTemplate().update(COMPOUND_EVENT_DETECTOR_UPDATE, ced.getXid(), ced.getName(), ced.getAlarmLevel().mangoId,
                boolToChar(ced.isReturnToNormal()), boolToChar(ced.isDisabled()), ced.getCondition(), ced.getId());

        eventManager.raiseChangedEvent(AuditEventType.TYPE_COMPOUND_EVENT_DETECTOR, old, ced);

    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteCompoundEventDetector(final int compoundEventDetectorId) {
        CompoundEventDetectorVO ced = getCompoundEventDetector(compoundEventDetectorId);
        if (ced != null) {
            getSimpleJdbcTemplate().update("delete from eventHandlers where eventTypeId=" + EventType.EventSources.COMPOUND
                    + " and eventTypeRef1=?", compoundEventDetectorId);
            getSimpleJdbcTemplate().update("delete from compoundEventDetectors where id=?", compoundEventDetectorId);
            eventManager.raiseDeletedEvent(AuditEventType.TYPE_COMPOUND_EVENT_DETECTOR, ced);
        }
    }
}
