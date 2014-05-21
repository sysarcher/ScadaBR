/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.web.mvc.controller;

import br.org.scadabr.logger.LogUtils;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.hierarchy.PointFolder;
import com.serotonin.mango.web.LazyTreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author aploese
 */
@Controller
public class PointHierarchyController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private DataPointDao dataPointDao;

    @RequestMapping(value = "/point_hierarchy.shtm")
    public String showForm() {
        return "pointHierarchy";
    }

    @RequestMapping(value = "/dstree", method = RequestMethod.GET)
    public @ResponseBody Object getNodeById(@RequestParam(value = "id", required = false) Integer id, @RequestParam(value = "parentId", required = false) Integer parentId) {
        if (id != null) {
            return dataPointDao.getFolderById(id);
        }
        if (parentId != null) {
            return dataPointDao.getFoldersAndDpByParentId(parentId);
        }
        return dataPointDao.getAllFoldersAndDp();
    }

    @RequestMapping("/dstree/root")
    public @ResponseBody LazyTreeNode getRootFolder() {
        return dataPointDao.getFolderById(0);
    }

    @RequestMapping("/dstree/{id}")
    public @ResponseBody LazyTreeNode getFolderNodeById(@PathVariable("id") int id) {
        LOG.severe("CALLED: getNodeById" + id);
        return dataPointDao.getFolderById(id);
    }

}
