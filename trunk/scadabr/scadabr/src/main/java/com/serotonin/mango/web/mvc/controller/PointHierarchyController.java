/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.serotonin.mango.web.mvc.controller;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.hierarchy.PointFolder;
import javax.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author aploese
 */
@Controller
@RequestMapping("/point_hierarchy.shtm")
public class PointHierarchyController {
    
    @Inject
    private DataPointDao dataPointDao;
    
    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap model) {
        return "pointHierarchy";
    }

    @RequestMapping(value = "/rootNode.json", method = RequestMethod.GET)
    public @ResponseBody PointFolder getRoot() {
        return dataPointDao.getRootFolder();
    }

    
}
