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
package br.org.scadabr.web.mvc.controller.datasources;

import br.org.scadabr.web.l10n.RequestContextAwareLocalizer;
import br.org.scadabr.web.mvc.AjaxFormPostResponse;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.DataSourceValidator;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope("request")
@RequestMapping("/dataSources/dataSource")
public class DataSourceController {

    @Inject
    private DataSourceDao dataSourceDao;
    @Inject
    @Autowired
    private transient RequestContextAwareLocalizer localizer;

    @ModelAttribute("dataSource")
    protected DataSourceVO getModel(int id) {
        return dataSourceDao.getDataSource(id);
    }

    @RequestMapping(params = "id", method = RequestMethod.GET)
    public String getDataSourceView(int id) {
        return "dataSources/dataSource";
    }

    @RequestMapping(params = "id", method = RequestMethod.POST)
    public @ResponseBody AjaxFormPostResponse postDataSource(@ModelAttribute("dataSource") DataSourceVO dataSource) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(dataSource, "dataSource");
        //TODO no autowire ??? why????
        DataSourceValidator validator = new DataSourceValidator(dataSourceDao);
        validator.validate(dataSource, bindingResult);
//        dataSource.createValidator().validate(dataSource, bindingResult);
        final AjaxFormPostResponse result = new AjaxFormPostResponse(bindingResult, localizer);
        if (!bindingResult.hasErrors()) {
            dataSourceDao.saveDataSource(dataSource);
        }
        return result;
    }

}
