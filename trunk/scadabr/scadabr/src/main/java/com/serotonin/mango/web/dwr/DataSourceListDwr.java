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
package com.serotonin.mango.web.dwr;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.DataSourceRegistry;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.web.dwr.DwrResponseI18n;

/**
 * @author Matthew Lohbihler
 */
public class DataSourceListDwr extends BaseDwr {

    @Autowired
    private RuntimeManager runtimeManager;
    @Autowired
    private DataSourceDao dataSourceDao;
    @Autowired
    private DataPointDao dataPointDao;

    public DwrResponseI18n init() {
        DwrResponseI18n response = new DwrResponseI18n();

        if (common.getUser().isAdmin()) {
            Map<DataSourceRegistry, String> dsTypes = new EnumMap(DataSourceRegistry.class);
            for (DataSourceRegistry type : DataSourceRegistry.values()) {
                // Allow customization settings to overwrite the default display value.
                if (type.isDisplay()) {
                    dsTypes.put(type, getMessage(type.getKey()));
                }
            }
            response.addData("dsTypes", dsTypes);
        }

        return response;
    }

    public Map<String, Object> toggleDataSource(int dataSourceId) {
        permissions.ensureDataSourcePermission(common.getUser(), dataSourceId);

        DataSourceVO<?> dataSource = runtimeManager.getDataSource(dataSourceId);
        Map<String, Object> result = new HashMap();

        dataSource.setEnabled(!dataSource.isEnabled());
        runtimeManager.saveDataSource(dataSource);

        result.put("enabled", dataSource.isEnabled());
        result.put("id", dataSourceId);
        return result;
    }

    public int deleteDataSource(int dataSourceId) {
        permissions.ensureDataSourcePermission(common.getUser(), dataSourceId);
        runtimeManager.deleteDataSource(dataSourceDao.getDataSource(dataSourceId));
        return dataSourceId;
    }

    public DwrResponseI18n toggleDataPoint(int dataPointId) {
        DataPointVO dataPoint = dataPointDao.getDataPoint(dataPointId);
        permissions.ensureDataSourcePermission(common.getUser(), dataPoint.getDataSourceId());

        dataPoint.setEnabled(!dataPoint.isEnabled());
        runtimeManager.saveDataPoint(dataPoint);

        DwrResponseI18n response = new DwrResponseI18n();
        response.addData("id", dataPointId);
        response.addData("enabled", dataPoint.isEnabled());
        return response;
    }

    public int copyDataSource(int dataSourceId) {
        permissions.ensureDataSourcePermission(common.getUser(), dataSourceId);
        int dsId = dataSourceDao.copyDataSource(dataSourceDao.getDataSource(dataSourceId), getResourceBundle());
        userDao.populateUserPermissions(common.getUser());
        return dsId;
    }
}
