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
package com.serotonin.mango.db;

import br.org.scadabr.ScadaBrVersionBean;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.jdbc.SystemSettingsDaoImpl;
import br.org.scadabr.dao.jdbc.UserDaoImpl;
import br.org.scadabr.db.spring.ConnectionCallbackVoid;
import br.org.scadabr.logger.LogUtils;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

abstract public class DatabaseAccess {

    private final Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_DAO_JDBC);
    protected final Properties jdbcProperties;

    protected DatabaseAccess(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    public void initialize(String scadaBrVersion) {
        initializeImpl("");

        JdbcTemplate ejt = new JdbcTemplate();
        ejt.setDataSource(getDataSource());

        try {
            if (newDatabaseCheck(ejt)) {
                // Check if we should convert from another database.
                final String convertTypeStr = jdbcProperties.getProperty("convert.db.type", "");

                if (!convertTypeStr.isEmpty()) {
                    // Found a database type from which to convert.
                    DatabaseType convertType = DatabaseType
                            .valueOf(convertTypeStr.toUpperCase());
                    if (convertType == null) {
                        throw new IllegalArgumentException(
                                "Unknown convert database type: " + convertType);
                    }

                    DatabaseAccess sourceAccess = convertType.getImpl(jdbcProperties);
                    sourceAccess.initializeImpl("convert.");

                    DBConvert convert = new DBConvert();
                    convert.setSource(sourceAccess);
                    convert.setTarget(this);
                    try {
                        convert.execute();
                    } catch (SQLException e) {
                        throw new ShouldNeverHappenException(e);
                    }

                    sourceAccess.terminate();
                } else {
                    LOG.info("Setup user admin in db");

                    // New database. Create a default user.
                    UserDaoImpl.createAdmin(ejt);

                    // Record the current version.
                    SystemSettingsDaoImpl.setSetSchemaVersion(ejt, scadaBrVersion);
                    LOG.info("database sucessfully created");

                }
            }
            // else
            // // The database exists, so let's make its schema version matches
            // // the application version.
            // DBUpgrade.checkUpgrade();
        } catch (CannotGetJdbcConnectionException e) {
            LOG.log(Level.SEVERE, "Unable to connect to database of type "
                    + getType().name(), e);
            throw e;
        }

        postInitialize(ejt);
    }

    abstract public DatabaseType getType();

    abstract public void terminate();

    abstract public DataSource getDataSource();

    abstract public double applyBounds(double value);

    abstract public void executeCompress(JdbcTemplate ejt);

    abstract protected void initializeImpl(String propertyPrefix);

    protected void postInitialize(
            @SuppressWarnings("unused") JdbcTemplate ejt) {
        // no op - override as necessary
    }

    abstract protected boolean newDatabaseCheck(JdbcTemplate ejt);

    abstract public void runScript(String[] script, final OutputStream out)
            throws Exception;

    public void doInConnection(ConnectionCallbackVoid callback) {
        DataSource dataSource = getDataSource();
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
            callback.doInConnection(conn);
            conn.commit();
        } catch (SQLException | CannotGetJdbcConnectionException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                LOG.log(Level.WARNING, "Exception during rollback", e1);
            }

            // Wrap and rethrow
            throw new ShouldNeverHappenException(e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

}
