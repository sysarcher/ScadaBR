/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.rest;

import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.web.LazyTreeNode;
import java.util.Collection;
import javax.inject.Inject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author aploese
 */
@RestController
@RequestMapping("/rest/pointHierarchy/")
public class RestPointHierarchyController {

    @Inject
    private DataPointDao dataPointDao;

    /**
     * get all child nodes (folders and datapoints) of the folder
     * @param parentId the folderId
     * @return All childnodes
     */
    @RequestMapping(params = "parentId", method = RequestMethod.GET)
    public Collection<LazyTreeNode> getChildNodesById(@RequestParam(value = "parentId", required = true) int parentId) {
            return dataPointDao.getFoldersAndDpByParentId(parentId);
    }

    /**
     * get the root of the pointHierarchy tree
     * @return the rood node
     */
    @RequestMapping(value = "root", method = RequestMethod.GET)
    public LazyTreeNode getRootFolder() {
        return dataPointDao.getRootFolder();
    }

    /**
     * Get folder node by its id
     * @param id of the node
     * @return the folder
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public LazyTreeNode getFolder(@PathVariable("id") int id) {
        return dataPointDao.getFolderById(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public LazyTreeNode putFolder(@PathVariable("id") int id, @RequestBody LazyTreeNode folder) {
        dataPointDao.savePointFolder(folder);
        return folder;
    }

    @RequestMapping(method = RequestMethod.POST)
    public LazyTreeNode postFolder(@RequestBody LazyTreeNode folder) {
        dataPointDao.savePointFolder(folder);
        return folder;
    }

}