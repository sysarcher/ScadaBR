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

import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.vo.dataSource.PointLocatorValidator;
import br.org.scadabr.vo.datasource.meta.MetaPointLocatorValidator;
import br.org.scadabr.web.l10n.RequestContextAwareLocalizer;
import br.org.scadabr.web.mvc.AjaxFormPostResponse;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope("request")
@RequestMapping("/dataSources/pointLocator")
public class PointLocatorController {

    @Inject
    private DataPointDao dataPointDao;
    @Inject
    private transient RequestContextAwareLocalizer localizer;

    @ModelAttribute("pointLocator")
    protected PointLocatorVO getModel(int id) {
        return dataPointDao.getDataPoint(id).getPointLocator();
    }

    @RequestMapping(params = "id", method = RequestMethod.GET)
    public String getPointLocatorView(@ModelAttribute("pointLocator") PointLocatorVO pointLocator) {
        return "dataSources/" + pointLocator.getClass().getSimpleName();
    }

    @RequestMapping(params = "id", method = RequestMethod.POST)
    public @ResponseBody AjaxFormPostResponse postPointLocator(@ModelAttribute("pointLocator") PointLocatorVO pointLocator) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(pointLocator, "pointLocator");
        //TODO no autowire ??? why????
        PointLocatorValidator validator = new MetaPointLocatorValidator(dataPointDao); // TOdo replace with factory ...
        validator.validate(pointLocator, bindingResult);
//        dataSource.createValidator().validate(dataSource, bindingResult);
        final AjaxFormPostResponse result = new AjaxFormPostResponse(bindingResult, localizer);
        if (!bindingResult.hasErrors()) {
            DataPointVO dp =  dataPointDao.getDataPoint(pointLocator.getId());
            dp.setPointLocator(pointLocator);
            dataPointDao.saveDataPoint(dp);
        }
        return result;
    }

}
