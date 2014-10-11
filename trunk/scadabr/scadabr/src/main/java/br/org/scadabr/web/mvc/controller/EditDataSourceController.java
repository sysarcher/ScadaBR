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
package br.org.scadabr.web.mvc.controller;

import br.org.scadabr.ShouldNeverHappenException;
import com.serotonin.mango.db.dao.DataSourceDao;
import javax.servlet.http.HttpServletRequest;

import com.serotonin.mango.vo.dataSource.DataSourceVO;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Scope("request")
@RequestMapping("/editDataSource")
public class EditDataSourceController {

    @Inject
    private DataSourceDao dataSourceDao;

    @RequestMapping(method = RequestMethod.GET)
    public String initializeForm(@RequestParam int id, ModelMap modelMap, HttpServletRequest request) {
        createModel(id, modelMap);
        return "dataSourceEdit/editFhz4J";
    }

    /**
     * Add all DataSourcesTypes
     *
     * @param modelMap
     */
    protected void createModel(int dsId, ModelMap modelMap) {
        final DataSourceVO dsvo = dataSourceDao.getDataSource(dsId);
        if (dsvo == null) {
            throw new ShouldNeverHappenException("DataSource not found with id " + dsId);
        }
        modelMap.addAttribute("dataSource", dsvo);
    }
}
