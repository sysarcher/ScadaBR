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

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;

/**
 * @author Matthew Lohbihler
 */
public class PublisherDao extends BaseDao {

    public String generateUniqueXid() {
        return generateUniqueXid(PublisherVO.XID_PREFIX, "publishers");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "publishers");
    }
    private static final String PUBLISHER_SELECT = "select id, xid, data from publishers ";

    public List<PublisherVO<? extends PublishedPointVO>> getPublishers() {
        return getSimpleJdbcTemplate().query(PUBLISHER_SELECT, new PublisherRowMapper());
    }

    public List<PublisherVO<? extends PublishedPointVO>> getPublishers(Comparator<PublisherVO<?>> comparator) {
        List<PublisherVO<? extends PublishedPointVO>> result = getPublishers();
        Collections.sort(result, comparator);
        return result;
    }

    public static class PublisherNameComparator implements Comparator<PublisherVO<?>> {

        @Override
        public int compare(PublisherVO<?> p1, PublisherVO<?> p2) {
            if (StringUtils.isEmpty(p1.getName())) {
                return -1;
            }
            return p1.getName().compareTo(p2.getName());
        }
    }

    public PublisherVO<? extends PublishedPointVO> getPublisher(int id) {
        return getSimpleJdbcTemplate().queryForObject(PUBLISHER_SELECT + " where id=?", new PublisherRowMapper(), id);
    }

    public PublisherVO<? extends PublishedPointVO> getPublisher(String xid) {
        return getSimpleJdbcTemplate().queryForObject(PUBLISHER_SELECT + " where xid=?", new PublisherRowMapper(), xid);
    }

    class PublisherRowMapper implements ParameterizedRowMapper<PublisherVO<? extends PublishedPointVO>> {

        @Override
        public PublisherVO<? extends PublishedPointVO> mapRow(ResultSet rs, int rowNum) throws SQLException {
            PublisherVO<? extends PublishedPointVO> p = (PublisherVO<? extends PublishedPointVO>) SerializationHelper.readObject(rs.getBlob(3).getBinaryStream());
            p.setId(rs.getInt(1));
            p.setXid(rs.getString(2));
            return p;
        }
    }

    public void savePublisher(final PublisherVO<? extends PublishedPointVO> vo) {
        // Decide whether to insert or update.
        if (vo.getId() == Common.NEW_ID) {
            SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("publishers").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("xid", vo.getXid());
            params.put("data", SerializationHelper.writeObjectToArray(vo));

            Number id = insertActor.executeAndReturnKey(params);
            vo.setId(id.intValue());
        } else {
            getSimpleJdbcTemplate().update("update publishers set xid=?, data=? where id=?", vo.getXid(),
                    SerializationHelper.writeObjectToArray(vo), vo.getId());
        }
    }

    public void deletePublisher(final int publisherId) {
        new TransactionTemplate(getTransactionManager()).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                getSimpleJdbcTemplate().update("delete from eventHandlers where eventTypeId=" + EventType.EventSources.PUBLISHER
                        + " and eventTypeRef1=?", publisherId);
                getSimpleJdbcTemplate().update("delete from publishers where id=?", publisherId);
            }
        });
    }

    public Object getPersistentData(int id) {
        return getJdbcTemplate().query("select rtdata from publishers where id=?", new Object[]{id},
                new ResultSetExtractor() {

                    @Override
                    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (!rs.next()) {
                            return null;
                        }

                        Blob blob = rs.getBlob(1);
                        if (blob == null) {
                            return null;
                        }

                        return SerializationHelper.readObjectInContext(blob.getBinaryStream());
                    }
                });
    }

    public void savePersistentData(int id, Object data) {
        getSimpleJdbcTemplate().update("update publishers set rtdata=? where id=?", SerializationHelper.writeObjectToArray(data), id);
    }
}
