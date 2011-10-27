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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonValue;
import com.serotonin.json.JsonWriter;
import com.serotonin.mango.Common;
import java.io.IOException;
import java.io.Reader;
import java.util.Calendar;
import java.util.TimeZone;
import org.springframework.stereotype.Repository;

/**
 * Base DAO class.
 * @author aploese
 */
@Repository
public class BaseDao {

    /** calendar to write and read timestamps in utc rather local time,
     * many driverser have problems with '2011-10-30 00:59:59.0000+0:00' which is '2011-10-30 02:59:59.0000 CEST' this timestamp plus 1 second is:
     * '2011-10-30 01:00:00.0000+0:00' or '2011-10-30 02:00:00.0000 CET'
     */
    protected final static Calendar CALENDAR_UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    /** the logger */
    private static final Logger LOG = LoggerFactory.getLogger(BaseDao.class);
    /** the jdbcTemplate to work with */
    private JdbcTemplate jdbcTemplate;
    /** the simpleJdbcTemplate to work with */
    private SimpleJdbcTemplate simpleJdbcTemplate;
    /** the data base type */
    private DataBaseTypes dataBaseType = null;

    /**
     * set teh dataSource to work with.
     * This is done by Dependency injection (DI)
     * @param dataSource the dataSource to set.
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = createJdbcTemplate(dataSource);
        simpleJdbcTemplate = new SimpleJdbcTemplate(getJdbcTemplate());
        dataBaseType = DataBaseTypes.getDataBaseType(getDataSource());
    }

    /**
     * @return the jdbcTemplate
     */
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * return the used dataSource
     * @return the underlying datatasource
     */
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    /**
     * create a new jdbcTemplate.
     * This should be done if one changes propertie (ie mawRows)
     *
     * @param dataSource the dataSource for the new jdbcTemplate.
     * @return the new created jdbcTemplate
     */
    protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * @return the simpleJdbcTemplate
     */
    protected SimpleJdbcTemplate getSimpleJdbcTemplate() {
        return simpleJdbcTemplate;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public int update(String sql) {
        return getJdbcTemplate().update(sql);
    }

    /**
     * @return the dataBaseType
     */
    public DataBaseTypes getDataBaseType() {
        return dataBaseType;
    }

    /**
     * Class that holds som properties specific to a concrete datatbase
     */
    public enum DataBaseTypes {

        /** Apache Derby */
        DERBY(false, 1000),
        /** HSQLDB */
        HSQLDB(true, -1),
        /** MySQL */
        MYSQL(true, 2000),
        /**MS SQL */
        MSSQL(true, 524);
        /** max rows ??? */
        public final int maxRows;
        /** is LIMIT supported in SQL */
        public final boolean limitSupported;

        /**
         * Constructor
         * @param limitSupported is LIMIT supported in SQL
         * @param maxRows number of max rows ???
         */ 
        private DataBaseTypes(boolean limitSupported, int maxRows) {
            this.limitSupported = limitSupported;
            this.maxRows = maxRows;
        }

        /**
         * Determin the database type by the meta data of the DataSource
         * @param ds the datasource to use
         * @return the data base type
         */
        public static DataBaseTypes getDataBaseType(DataSource ds) {
            try (Connection conn = ds.getConnection()) {
                final DatabaseMetaData dbMeta = conn.getMetaData();
                switch (dbMeta.getDriverName()) {
                    case "HSQL Database Engine Driver":
                        return HSQLDB;
                    case "MySQL-AB JDBC Driver":
                        return MYSQL;
                    case "Apache Derby Embedded JDBC Driver":
                        return DERBY;
                    default:
                        throw new ShouldNeverHappenException("Unknown Database driver " + dbMeta.getDriverName());
                }
            } catch (SQLException ex) {
                throw new ShouldNeverHappenException(ex);
            } finally {
            }
        }
    }

    /**
     * is LIMIT supported in SQL
     * @return true, if  LIMIT is supported.
     */
    protected boolean isLimitSupported() {
        return DataBaseTypes.getDataBaseType(getDataSource()).limitSupported;
    }

    protected int getMaxRowns() {
        return DataBaseTypes.getDataBaseType(getDataSource()).maxRows;
    }

    //** Convenience methods for storage of booleans. */
    @Deprecated
    protected static String boolToChar(boolean b) {
        return b ? "Y" : "N";
    }

    @Deprecated
    protected static boolean charToBool(String s) {
        return "Y".equals(s);
    }

    /**
     * Generate a unique xid
     * @param prefix
     * @param tableName
     * @return 
     */
    protected String generateUniqueXid(String prefix, String tableName) {
        String xid = Common.generateXid(prefix);
        while (!isXidUnique(xid, Common.NEW_ID, tableName)) {
            xid = Common.generateXid(prefix);
        }
        return xid;
    }

    /**
     * Checks if given xid is unique for a given table and the id of the xid.
     * @param xid the xid to test
     * @param excludeId the id of the row
     * @param tableName the name of the table
     * @return true, if the xid is unique otherwis false..
     */
    @Transactional(readOnly = true)
    protected boolean isXidUnique(String xid, int excludeId, String tableName) {
        return getJdbcTemplate().queryForInt(String.format("select count(*) from %s where xid=? and id<>?", tableName), xid,
                    excludeId) == 0;
    }

    /**
     * Use named params
     * @param values
     * @param delimeter
     * @param quote
     * @return
     * @deprecated
     */
    @Deprecated
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

    /**
     * use named params
     * @param values
     * @param from
     * @param to
     * @param delimeter
     * @param quote
     * @return 
     */
    @Deprecated
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
    /**
     * Set max rows of a new created JdbcTempülate to limit
     * @param limit the max number of rows to be returned
     * @return the new created JdbcTemplae
     */
    protected SimpleJdbcTemplate getLimitJdbcTemplate(int limit) {
        JdbcTemplate tmpl = createJdbcTemplate(getDataSource());
        tmpl.setMaxRows(limit);
        return new SimpleJdbcTemplate(tmpl);
    }

    /**
     * Maybe Some old stuff???
     * @param callback the callback.
     * @deprecated
     */
    @Deprecated
    public void doInConnection(ConnectionCallback callback) {
        DataSource dataSource = getDataSource();
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
            callback.doInConnection(conn);
            conn.commit();
        } catch (SQLException | DataAccessException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                LOG.warn("Exception during rollback", e1);
            }

            // Wrap and rethrow
            throw new ShouldNeverHappenException(e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
 
    /**
     * Helper to deserialize Beans.
     * @param json the JsonReader.
     * @param o the object to be deserialized ("filled").
     * @throws JsonException 
     */
    protected void jsonDeserialize(Reader json, Object o) throws JsonException {
        JsonReader reader = new JsonReader(json);
        JsonValue value = reader.inflate();
        JsonObject root = value.toJsonObject();
        reader.populateObject(o, root);
    }

    /**
     * Helper to serialize Beans
     * @param o the object to be serialized
     * @return a String that represents the json serialized Object.
     */
    protected String jsonSerialize(Object o) {
        JsonWriter writer = new JsonWriter();
        writer.setPrettyIndent(1);
        writer.setPrettyOutput(true);

        try {
            return writer.write(o);
        } catch (JsonException | IOException e) {
            throw new ShouldNeverHappenException(e);
        }
    }
}
