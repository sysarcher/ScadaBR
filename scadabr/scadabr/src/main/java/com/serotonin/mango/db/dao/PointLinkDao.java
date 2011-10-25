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

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.link.PointLinkVO;

/**
 * @author Matthew Lohbihler
 */
@Service
public class PointLinkDao extends BaseDao {

    @Autowired
    private EventManager eventManager;

    public String generateUniqueXid() {
        return generateUniqueXid(PointLinkVO.XID_PREFIX, "pointLinks");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "pointLinks");
    }
    private static final String POINT_LINK_SELECT = "select id, xid, sourcePointId, targetPointId, script, eventType, disabled from pointLinks ";

    public List<PointLinkVO> getPointLinks() {
        return getSimpleJdbcTemplate().query(POINT_LINK_SELECT, new PointLinkRowMapper());
    }

    public List<PointLinkVO> getPointLinksForPoint(int dataPointId) {
        return getSimpleJdbcTemplate().query(POINT_LINK_SELECT + "where sourcePointId=? or targetPointId=?", new PointLinkRowMapper(), dataPointId,
                dataPointId);
    }

    public PointLinkVO getPointLink(int id) {
        return getSimpleJdbcTemplate().queryForObject(POINT_LINK_SELECT + "where id=?", new PointLinkRowMapper(), id);
    }

    public PointLinkVO getPointLink(String xid) {
        return getSimpleJdbcTemplate().queryForObject(POINT_LINK_SELECT + "where xid=?", new PointLinkRowMapper(), xid);
    }

    class PointLinkRowMapper implements ParameterizedRowMapper<PointLinkVO> {

        @Override
        public PointLinkVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            PointLinkVO pl = new PointLinkVO();
            int i = 0;
            pl.setId(rs.getInt(++i));
            pl.setXid(rs.getString(++i));
            pl.setSourcePointId(rs.getInt(++i));
            pl.setTargetPointId(rs.getInt(++i));
            pl.setScript(rs.getString(++i));
            pl.setEvent(rs.getInt(++i));
            pl.setDisabled(charToBool(rs.getString(++i)));
            return pl;
        }
    }

    public void savePointLink(final PointLinkVO pl) {
        if (pl.getId() == Common.NEW_ID) {
            insertPointLink(pl);
        } else {
            updatePointLink(pl);
        }
    }
    private static final String POINT_LINK_INSERT = "insert into pointLinks (xid, sourcePointId, targetPointId, script, eventType, disabled) "
            + "values (?,?,?,?,?,?)";

    private void insertPointLink(PointLinkVO pl) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("pointLinks").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", pl.getXid());
        params.put("sourcePointId", pl.getSourcePointId());
        params.put("targetPointId", pl.getTargetPointId());
        params.put("script", pl.getScript());
        params.put("eventType", pl.getEvent());
        params.put("disabled", boolToChar(pl.isDisabled()));

        Number id = insertActor.executeAndReturnKey(params);
        pl.setId(id.intValue());

        eventManager.raiseAddedEvent(AuditEventType.TYPE_POINT_LINK, pl);
    }
    private static final String POINT_LINK_UPDATE = "update pointLinks set xid=?, sourcePointId=?, targetPointId=?, script=?, eventType=?, disabled=? "
            + "where id=?";

    private void updatePointLink(PointLinkVO pl) {
        PointLinkVO old = getPointLink(pl.getId());

        getSimpleJdbcTemplate().update(POINT_LINK_UPDATE, pl.getXid(), pl.getSourcePointId(), pl.getTargetPointId(), pl.getScript(),
                pl.getEvent(), boolToChar(pl.isDisabled()), pl.getId());

        eventManager.raiseChangedEvent(AuditEventType.TYPE_POINT_LINK, old, pl);
    }

    public void deletePointLink(final int pointLinkId) {
        PointLinkVO pl = getPointLink(pointLinkId);
        if (pl != null) {
            getSimpleJdbcTemplate().update("delete from pointLinks where id=?", pointLinkId);
            eventManager.raiseDeletedEvent(AuditEventType.TYPE_POINT_LINK, pl);
        }
    }
}
