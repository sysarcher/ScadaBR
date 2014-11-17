/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.datasources;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.web.l10n.RequestContextAwareLocalizer;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.UserSessionContextBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for the DataSources Tree
 * @author aploese
 */
@RestController
@RequestMapping(value = "/dataSources/dsTree/")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DsTreeController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private UserSessionContextBean userSessionContextBean;
    @Inject
    private RuntimeManager runtimeManager;
    @Inject
    private DataSourceDao dataSourceDao;
    @Inject
    private DataPointDao dataPointDao;
    
    @Inject 
    private RequestContextAwareLocalizer localizer;


    @RequestMapping(method = RequestMethod.GET)
    public List<JsonDataSource> getDataSources() {
        LOG.severe("CALLED: getDataSources");
        
        final User user = userSessionContextBean.getUser();
        List<JsonDataSource> result = new ArrayList<>();
        for (DataSourceVO<?> ds : runtimeManager.getDataSources()) {
            if (Permissions.hasDataSourcePermission(user, ds.getId())) {
                result.add(new JsonDataSource(ds, localizer));
            }
        }
        return result;
    }
    
    /**
     * Get folder node by its id
     * @param id of the node
     * @return the folder
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public JsonDataSource getDataSource(@PathVariable("id") int id) {
        return new JsonDataSource(dataSourceDao.getDataSource(id), localizer);
    }

    /**
     * get all child nodes (folders and datapoints) of the folder
     * @return All childnodes
     */
    @RequestMapping(params = {"dsId", "parentFolderId"}, method = RequestMethod.GET)
    public List<JsonPointLocator> getPointLocators(int dsId, int parentFolderId) {
        List<DataPointVO> dps = dataPointDao.getDataPoints(dsId, null);
        List<JsonPointLocator> result = new LinkedList<>();
        for (DataPointVO dp : dps) {
            result.add(new JsonPointLocator(dp.getPointLocator(), localizer));
        }
        return result;
    }

}
