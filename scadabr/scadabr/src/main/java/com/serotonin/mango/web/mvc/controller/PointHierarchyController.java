/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.serotonin.mango.web.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author aploese
 */
@Controller
@RequestMapping("/point_hierarchy.shtm")
public class PointHierarchyController {
    
    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap model) {
        return "pointHierarchy";
    }

    
}
