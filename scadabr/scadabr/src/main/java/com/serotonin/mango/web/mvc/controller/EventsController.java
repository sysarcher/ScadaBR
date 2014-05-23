/*
 Mango - Open Source M2M - http://mango.serotoninsoftware.com
 Copyright (C) 2006-2009 Serotonin Software Technologies Inc.
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
package com.serotonin.mango.web.mvc.controller;


import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.EventDao;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EventsController {

    @Inject
    private EventDao eventDao;
    
    @RequestMapping(value  = "/events.shtm")
    public String showForm(Model model) {
        return "events";
    }
    
        @RequestMapping(value = "/events", method = RequestMethod.GET)
    public @ResponseBody Object getNodeById(HttpServletRequest request, @RequestParam(value = "id", required = false) Integer id) {
        if (id != null) {
            return eventDao.getEventInstance(id);
        }
        return eventDao.getPendingEvents(Common.getUser(request).getId());
    }


}
