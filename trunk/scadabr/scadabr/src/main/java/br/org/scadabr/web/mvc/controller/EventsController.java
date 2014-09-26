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
package br.org.scadabr.web.mvc.controller;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.web.UserSessionContextBean;
import br.org.scadabr.web.mvc.controller.jsonrpc.JsonEventInstance;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope("request")
public class EventsController {

    private static final Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private EventDao eventDao;

    @Inject
    private Localizer localizer;

    @Inject
    private UserSessionContextBean userSessionContextBean;

    @RequestMapping(value = "/events.shtm")
    public String showForm(Model model) {
        return "events";
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public @ResponseBody
    Object getNodeById(HttpServletRequest request, @RequestParam(value = "id", required = false) Integer id) {
        if (id != null) {
            return JsonEventInstance.wrap(eventDao.getEventInstance(id), localizer);
        }
        return JsonEventInstance.wrap(eventDao.getPendingEvents(userSessionContextBean.getUser()), localizer);
    }

}
