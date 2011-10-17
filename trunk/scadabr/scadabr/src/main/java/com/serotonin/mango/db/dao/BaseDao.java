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

import java.util.List;

import javax.sql.DataSource;

import com.serotonin.mango.Common;
import java.util.Collection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class BaseDao extends SimpleJdbcDaoSupport {

    private DataSourceTransactionManager dsTm;

    /**
     * Public constructor for code that needs to get stuff from the database.
     */
    @Deprecated
    public BaseDao() {
        super();
        setDataSource(Common.ctx.getDatabaseAccess().getDataSource());

    }

    @Deprecated
    protected BaseDao(DataSource dataSource) {
        super();
        setDataSource(dataSource);

    }

    @Deprecated // Uses annotations ???
    protected synchronized DataSourceTransactionManager getTransactionManager() {
        if (dsTm == null) {
            dsTm = new DataSourceTransactionManager(getDataSource());
        }
        return dsTm;
    }
    //
    // Convenience methods for storage of booleans.
    //
    protected static String boolToChar(boolean b) {
        return b ? "Y" : "N";
    }

    protected static boolean charToBool(String s) {
        return "Y".equals(s);
    }

    protected void deleteInChunks(String sql, List<Integer> ids) {
        int chunk = 1000;
        for (int i = 0; i < ids.size(); i += chunk) {
            String idStr = createDelimitedList(ids, i, i + chunk, ",", null);
            getJdbcTemplate().update(sql + " (" + idStr + ")");
        }
    }

    //
    // XID convenience methods
    //
    protected String generateUniqueXid(String prefix, String tableName) {
        String xid = Common.generateXid(prefix);
        while (!isXidUnique(xid, -1, tableName)) {
            xid = Common.generateXid(prefix);
        }
        return xid;
    }

    protected boolean isXidUnique(String xid, int excludeId, String tableName) {
        return getJdbcTemplate().queryForInt("select count(*) from " + tableName + " where xid=? and id<>?", new Object[]{xid,
                    excludeId}) == 0;
    }

    protected String createDelimitedList(Collection<?> values, String delimeter, String quote) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                sb.append(delimeter);
            }
            if (quote != null) {
                sb.append(quote);
                sb.append(value);
                sb.append(quote);
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    protected String createDelimitedList(List<?> values, int from, int to, String delimeter, String quote) {
        if (from < 0) {
            from = 0;
        }
        if (to > values.size()) {
            to = values.size();
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = from; i < to; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(delimeter);
            }
            if (quote != null) {
                sb.append(quote);
                sb.append(values.get(i));
                sb.append(quote);
            } else {
                sb.append(values.get(i));
            }
        }
        return sb.toString();
    }

    //TODO check if limit is supported otherwise this .,..
    protected SimpleJdbcTemplate getLimitJdbcTemplate(int limit) {
        JdbcTemplate tmpl = createJdbcTemplate(getDataSource());
        tmpl.setMaxRows(limit);
        return new SimpleJdbcTemplate(tmpl);
    }
}
