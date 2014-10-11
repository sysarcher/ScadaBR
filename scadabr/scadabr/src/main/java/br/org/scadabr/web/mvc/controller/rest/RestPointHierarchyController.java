/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.rest;

import br.org.scadabr.web.mvc.controller.*;
import br.org.scadabr.logger.LogUtils;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.web.LazyTreeNode;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author aploese
 */
@RestController
@Scope("request")
public class RestPointHierarchyController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private DataPointDao dataPointDao;

    @RequestMapping(value = "/rest/pointHierarchy", params = "id", method = RequestMethod.GET)
    public Object getNodeById(@RequestParam(value = "id", required = true) Integer id) {
        LOG.severe("CALLED: getNodeById" + id);
            return dataPointDao.getFolderById(id);
    }

    @RequestMapping(value = "/rest/pointHierarchy", params = "parentId", method = RequestMethod.GET)
    public Object getChildNodesById(@RequestParam(value = "parentId", required = true) Integer parentId) {
        LOG.severe("CALLED: getChildNodesById" + parentId);
            return dataPointDao.getFoldersAndDpByParentId(parentId);
    }

    @RequestMapping("/rest/pointHierarchy/root")
    public LazyTreeNode getRootFolder() {
        LOG.severe("CALLED: getRootFolder");
        return dataPointDao.getFolderById(0);
    }

    @RequestMapping("/rest/pointHierarchy/{id}")
    public LazyTreeNode getFolderNodeById(@PathVariable("id") int id) {
        LOG.severe("CALLED: getFolderNodeById" + id);
        return dataPointDao.getFolderById(id);
    }

}