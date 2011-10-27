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

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.mango.Common;
import com.serotonin.mango.SysProperties;

@Repository
public class SystemSettingsDao extends BaseDao {

    private final Map<String, String> cache = new HashMap<>();

    @Override
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        for (SysProperties prop : SysProperties.values()) {
            cache.put(prop.key, prop.defaultValue);

        }
        getJdbcTemplate().query("select settingName, settingValue from systemSettings", new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                cache.put(rs.getString("settingName"), rs.getString("settingValue"));
            }
        });
    }

    public String getValue(SysProperties prop) {
        return cache.get(prop.key);
    }

    private String getValue(String key) {
        return cache.get(key);
    }

    public String getValue(String key, String defaultValue) {
        final String result = cache.get(key);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public int getIntValue(SysProperties prop) {
        return Integer.parseInt(getValue(prop.key));
    }

    public int getIntValue(String key, int defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public boolean getBooleanValue(SysProperties prop) {
        return Boolean.valueOf(getValue(prop.key));
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = getValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public void setValue(final SysProperties prop, final String value) {
        setValue(prop.key, value);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void setValue(final String key, final String value) {
        // Delete any existing value.
        removeValue(key);

        // Insert the new value if it's not null.
        if (value != null) {
            getSimpleJdbcTemplate().update(
                    "insert into systemSettings values (?,?)", key, value);
        }

        // Update the cache
        cache.put(key, value);

    }

    public void setIntValue(SysProperties prop, int value) {
        setIntValue(prop.key, value);
    }

    public void setIntValue(String key, int value) {
        setValue(key, Integer.toString(value));
    }

    public void setBooleanValue(SysProperties prop, boolean value) {
        setBooleanValue(prop.key, value);
    }

    public void setBooleanValue(String key, boolean value) {
        setValue(key, Boolean.toString(value));
    }

    /**
     * Remove value from cach and dataBase.
     * If the valuze is a #SysProperties then the value will be set to the default value and the database entry will be deleted.
     * @param key the key of the setting.
     */
    public void removeValue(String key) {
        try {
            SysProperties prop = SysProperties.valueOf(key);
            cache.put(prop.key, prop.defaultValue);
        } catch (Exception ex) {
            // Remove the value from the cache
            cache.remove(key);
        }

        // Reset the cached values too.
        futureDateLimit = -1;

        getSimpleJdbcTemplate().update("delete from systemSettings where settingName=?", key);
    }

    //TODO whats this for???
    public long getFutureDateLimit() {
        if (futureDateLimit == -1) {
            futureDateLimit = Common.getMillis(
                    Common.TIME_PERIOD_CODES.getId(getValue(SysProperties.FUTURE_DATE_LIMIT_PERIOD_TYPE)),
                    getIntValue(SysProperties.FUTURE_DATE_LIMIT_PERIODS));
        }
        return futureDateLimit;
    }

    public Color getColor(SysProperties prop) {
        return new Color(Long.decode(getValue(prop.key)).intValue(), true);
    }
    /**
     * Special caching for the future dated values property, which needs high
     * performance.
     */
    private static long futureDateLimit = -1;

    //TODO move to emport???
    @Deprecated
    public void resetDataBase() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
