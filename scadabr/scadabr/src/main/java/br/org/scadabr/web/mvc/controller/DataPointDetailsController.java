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


import br.org.scadabr.logger.LogUtils;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.vo.DataPointVO;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;

import javax.validation.Valid;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dataPointDetails")
@Scope("request")
class DataPointDetailsController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);
    
    @Inject
    private DataPointDao dataPointDao;
    
    @Inject
    private RuntimeManager runtimeManager;
    

    public DataPointDetailsController() {
        super();
    }
    
    @RequestMapping(value="/editCommonProperties", method = RequestMethod.GET)
    protected String getEditCommonProperties(@RequestParam int id, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.severe("showForm called "+ id);
        return "dataPointDetails/editCommonProperties";
    }

    @RequestMapping(value="/editCommonProperties", method = RequestMethod.POST)
    protected String postEditCommonProperties(@RequestParam int id, @ModelAttribute("dataPoint") @Valid DataPointVO dataPoint, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) throws BindException {
        LOG.severe("onSubmit called "+ id);
        if (bindingResult.hasErrors()) {
            return "dataPointDetails/editCommonProperties";
        }
        runtimeManager.saveDataPoint(dataPoint);
        return "dataPointDetails/editCommonProperties";
    }
    
    @ModelAttribute
    protected void getModel(@RequestParam int id, Model model) {
        LOG.severe("getModel called "+ id);
        final DataPointVO dataPointVO = dataPointDao.getDataPoint(id);
        model.addAttribute("dataPoint", dataPointVO);
    }

    @RequestMapping(value="/renderChart", method = RequestMethod.GET)
    protected String getRrenderChart(@RequestParam int id, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.severe("showForm called "+ id);
        return "dataPointDetails/renderChart";
    }

    
    
}
