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


import br.org.scadabr.web.mvc.form.LoginForm;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;

import javax.validation.Valid;
import net.sf.openv4j.DataPoint;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pointEdit/common")
@Scope("request")
class PointEditController {
    
    @Inject
    private DataPointDao dataPointDao;
    
    private static final Log logger = LogFactory.getLog(PointEditController.class);

    public PointEditController() {
        super();
    }
    
 
    @RequestMapping(method = RequestMethod.GET)
    protected String showForm(@RequestParam int id, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final DataPointVO dataPointVO = dataPointDao.getDataPoint(id);
        model.addAttribute("dataPoint", dataPointVO);
        return "pointEdit/common";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String onSubmit(@ModelAttribute("login") @Valid LoginForm loginForm, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) throws BindException {
        if (bindingResult.hasErrors()) {
            return "pointEdit/common";
        }
        // store changes
        return "pointEdit/common";
    }

}
