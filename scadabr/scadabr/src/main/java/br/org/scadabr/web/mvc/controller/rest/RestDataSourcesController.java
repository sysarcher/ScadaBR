/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.rest;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.web.l10n.Localizer;
import br.org.scadabr.web.mvc.controller.jsonrpc.JsonWatchList;
import br.org.scadabr.web.mvc.controller.jsonrpc.JsonWatchListPoint;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.UserSessionContextBean;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author aploese
 */
@Controller
@Scope("request")
public class RestDataSourcesController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private UserSessionContextBean userSessionContextBean;
    @Inject
    private RuntimeManager runtimeManager;
    @Inject
    private DataPointDao dataPointDao;
    
    @Inject 
    private Localizer localizer;


    @RequestMapping(value = "/rest/dataSources", method = RequestMethod.GET)
    public @ResponseBody List<JsonDataSource> getDataSources() {
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


}
