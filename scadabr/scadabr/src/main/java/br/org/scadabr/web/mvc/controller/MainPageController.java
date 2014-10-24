/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller;

import static com.serotonin.mango.web.dwr.MiscDwr.LOG;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handle misc stuff from page.tag
 * @author aploese
 */
@Controller
@Scope("request")
public class MainPageController {
        
    @RequestMapping(value = "/javaScriptError", method = RequestMethod.GET)
    public void jsError(String desc, String page, String line, String browserName, String browserVersion,
            String osName, String location) {
        LOG.warn("Javascript error\r\n" + "   Description: " + desc + "\r\n" + "   Page: " + page + "\r\n"
                + "   Line: " + line + "\r\n" + "   Browser name: " + browserName + "\r\n" + "   Browser version: "
                + browserVersion + "\r\n" + "   osName: " + osName + "\r\n" + "   location: " + location);
    }

}
