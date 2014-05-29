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
package com.serotonin.mango.web.mvc.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.web.UserSessionContextBean;
import com.serotonin.mango.web.integration.CrowdUtils;
import com.serotonin.mango.web.mvc.form.LoginForm;
import javax.inject.Inject;
import javax.validation.Valid;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login.htm")
@Scope("request")
public class LoginController {
    
    private static final Log logger = LogFactory.getLog(LoginController.class);

    private String successUrl ="redirect:watch_list.shtm";
    private String newUserUrl = "redirect:help.shtm";
    private String loginView = "login";
    @Inject
    private UserDao userDao;
	
   @Inject
    private UserSessionContextBean userSessionContextBean;

    public LoginController() {
        super();
    }
    
 
    public void setSuccessUrl(String url) {
        successUrl = url;
    }

    public void setNewUserUrl(String newUserUrl) {
        this.newUserUrl = newUserUrl;
    }

    @RequestMapping(method = RequestMethod.GET)
    protected String showForm(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            // Check if Crowd is enabled
        if (CrowdUtils.isCrowdEnabled()) {
            String username = CrowdUtils.getCrowdUsername(request);

            if (username != null) {
                model.addAttribute("login", new LoginForm(username));

                // The user is logged into Crowd. Make sure the username is valid in this instance.
                User user = userDao.getUser(username);
                if (user == null) {
//                    ValidationUtils.rejectValue(errors, "username", "login.validation.noSuchUser");
                } else {
                    // Validate some stuff about the user.
                    if (user.isDisabled()) {
//                        ValidationUtils.reject(errors, "login.validation.accountDisabled");
                    } else {
                        if (CrowdUtils.isAuthenticated(request, response)) {
                            String result = performLogin(username);
                            CrowdUtils.setCrowdAuthenticated(Common.getUser(request));
                            return result;
                        }
                    }
                }
            }
        } else {
            model.addAttribute("login", new LoginForm());
        }
        return loginView;
    }

    /*
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) {
        LoginForm login = (LoginForm) command;

        // Make sure there is a username
        if (login.getUsername().isEmpty()) {
            ValidationUtils.rejectValue(errors, "username", "login.validation.noUsername");
        }

        // Make sure there is a password
        if (login.getPassword().isEmpty()) {
            ValidationUtils.rejectValue(errors, "password", "login.validation.noPassword");
        }
    }
*/
    //TODO @Valid does not work with <spring:bind ????
    
    @RequestMapping(method = RequestMethod.POST)
    public String onSubmit(@ModelAttribute("login") @Valid LoginForm loginForm, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) throws BindException {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        
        boolean crowdAuthenticated = false;

        // Check if the user exists
        User user = userDao.getUser(loginForm.getUsername());
        if (user == null) {
//            ValidationUtils.rejectValue(errors, "username", "login.validation.noSuchUser");
        } else if (user.isDisabled()) {
//            ValidationUtils.reject(errors, "login.validation.accountDisabled");
        } else {
            if (CrowdUtils.isCrowdEnabled()) {
                // First attempt authentication with Crowd.
                crowdAuthenticated = CrowdUtils.authenticate(request, response, loginForm.getUsername(),
                        loginForm.getPassword());
            }

            if (!crowdAuthenticated) {
                String passwordHash = Common.encrypt(loginForm.getPassword());

                // Validating the password against the database.
                if (!passwordHash.equals(user.getPassword())) {
//                    ValidationUtils.reject(errors, "login.validation.invalidLogin");
                }
            }
        }
/*
        if (errors.hasErrors()) {
            return "login";
        }
*/
        String result = performLogin(loginForm.getUsername());
        if (crowdAuthenticated) {
            CrowdUtils.setCrowdAuthenticated(userSessionContextBean.getUser());
        }
        return result;
    }

    private String performLogin(String username) {
        // Check if the user is already logged in.
        User user = userSessionContextBean.getUser();
        if (user != null && user.getUsername().equals(username)) {
            // The user is already logged in. Nothing to do.
            if (logger.isDebugEnabled()) {
                logger.debug("User is already logged in, not relogging in");
            }
        } else {
            // Get the user data from the app server.
            user = userDao.getUser(username);
            if (user == null) {
                return "redirect:login.htm";
            }
            // Update the last login time.
            userDao.recordLogin(user.getId());

            // Add the user object to the session. This indicates to the rest
            // of the application whether the user is logged in or not.
            userSessionContextBean.setUser(user);
            if (logger.isDebugEnabled()) {
                logger.debug("User object added to session");
            }
        }

            if (user.isFirstLogin()) {
                return newUserUrl;
            }
            if (!user.getHomeUrl().isEmpty()) {
                return "redirect:" + user.getHomeUrl();
            }

        return successUrl;
    }

}
