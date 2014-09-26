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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.serotonin.mango.vo.User;
import com.serotonin.mango.web.UserSessionContextBean;
import com.serotonin.mango.web.integration.CrowdUtils;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
@RequestMapping("/logout.htm")
@Scope("request")
public class LogoutController  {

    @Inject
    private UserSessionContextBean userSessionContextBean;
    
    private String redirectUrl;

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @RequestMapping()
    protected String handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        // Check if the user is logged in.
        User user = userSessionContextBean.getUser();
        if (user != null) {
            // The user is in fact logged in. Invalidate the session.
            request.getSession().invalidate();
            userSessionContextBean.logoutUser(user);
            
            if (CrowdUtils.isCrowdEnabled()) {
                CrowdUtils.logout(request, response);
            }
        }
        
        // Regardless of what happened above, forward to the configured view.
        return "redirect:login.htm";
    }
}
