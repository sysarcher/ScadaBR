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

import br.org.scadabr.web.jsonrpc.JsonRpcRequest;
import br.org.scadabr.web.jsonrpc.JsonRpcResponse;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.User;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EventsController {

    private JsonEventInstance wrap(EventInstance eventInstance, ResourceBundle bundle, DateFormat df) {
        final JsonEventInstance result = new JsonEventInstance();
        result.setId(eventInstance.getId());
        result.setActive(eventInstance.isActive());
        result.setAlarmLevel(eventInstance.getAlarmLevel());
        result.setActiveTimestamp(df.format(new Date(eventInstance.getActiveTimestamp())));
        if (eventInstance.getRtnTimestamp() == 0) {
            result.setRtnTimestamp("");
        } else {
            result.setActiveTimestamp(df.format(new Date(eventInstance.getActiveTimestamp())));
        }
        result.setMessage(Localizer.localizeMessage(eventInstance.getMessage(), bundle));
        return result;
    }

    private Collection<JsonEventInstance> wrap(Collection<EventInstance> eventInstances, ResourceBundle bundle, DateFormat df) {
        List<JsonEventInstance> result = new ArrayList<>(eventInstances.size());
        for (EventInstance ei : eventInstances) {
            result.add(wrap(ei, bundle, df));
        }
        return result;
    }

    public static class JsonEventInstance {

        private int id;
        private int alarmLevel;
        private boolean active;
        private String activeTimestamp;
        private String rtnTimestamp;
        private String message;

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return the alarmLevel
         */
        public int getAlarmLevel() {
            return alarmLevel;
        }

        /**
         * @param alarmLevel the alarmLevel to set
         */
        public void setAlarmLevel(int alarmLevel) {
            this.alarmLevel = alarmLevel;
        }

        /**
         * @return the activeTimestamp
         */
        public String getActiveTimestamp() {
            return activeTimestamp;
        }

        /**
         * @param activeTimestamp the activeTimestamp to set
         */
        public void setActiveTimestamp(String activeTimestamp) {
            this.activeTimestamp = activeTimestamp;
        }

        /**
         * @return the rtnTimestamp
         */
        public String getRtnTimestamp() {
            return rtnTimestamp;
        }

        /**
         * @param rtnTimestamp the rtnTimestamp to set
         */
        public void setRtnTimestamp(String rtnTimestamp) {
            this.rtnTimestamp = rtnTimestamp;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @param message the message to set
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @return the active
         */
        public boolean isActive() {
            return active;
        }

        /**
         * @param active the active to set
         */
        public void setActive(boolean active) {
            this.active = active;
        }
    }

    @Inject
    private EventDao eventDao;

    @RequestMapping(value = "/events.shtm")
    public String showForm(Model model) {
        return "events";
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public @ResponseBody
    Object getNodeById(HttpServletRequest request, @RequestParam(value = "id", required = false) Integer id) {
        DateFormat df = DateFormat.getDateTimeInstance();
        if (id != null) {
            return wrap(eventDao.getEventInstance(id), ControllerUtils.getResourceBundle(request), df);
        }
        return wrap(eventDao.getPendingEvents(Common.getUser(request).getId()), ControllerUtils.getResourceBundle(request), df);
    }

    @RequestMapping(value = "/events/rpc", method = RequestMethod.POST)
    public @ResponseBody
    JsonRpcResponse<?, ?> rpc(JsonRpcRequest jsonRequest, HttpServletRequest request) {
        try {
            User user = Common.getUser(request);
            DateFormat df = DateFormat.getDateTimeInstance();
            if (user != null) {
                long now = System.currentTimeMillis();
                for (EventInstance evt : eventDao.getPendingEvents(user.getId())) {
                    eventDao.ackEvent(evt.getId(), now, user.getId(), 0);
                }
//TODO impl            MiscDWR.resetLastAlarmLevelChange();
            }
            return JsonRpcResponse.createSuccessResponse(
                    jsonRequest.getId(), 
                    wrap(eventDao.getPendingEvents(Common.getUser(request).getId()), ControllerUtils.getResourceBundle(request), df));
        } catch (Throwable t) {
            return JsonRpcResponse.createErrorResponse(jsonRequest.getId(), t);
        }
    }

    @RequestMapping(value = "/events.smd")
    public @ResponseBody
    String getSmd() {
        return "{\n"
                + "  serviceUrl: 'events/rpc/', // Adress of the RPC service end point\n"
                + "  timeout: 1000, // Only used if an object is passed to the constructor (!)\n"
                + "  // Only used if an object is passed to the constructor (!)\n"
                + "  // if true, parameter count of each method will be checked against the\n"
                + "  // length of its description's 'parameters' attribute\n"
                + "  strictArgChecks: true,\n"
                + "  // Methods descriptions\n"
                + "  methods: [{\n"
                + "     name: 'acknowledgeAllPendingEvents',\n"
                + "  }]\n"
                + "};";
    }

}
