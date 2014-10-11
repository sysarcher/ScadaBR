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
import java.util.logging.Logger;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author aploese
 */
@RestController
@Scope("request")
public class RestWatchListController {

    private static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private transient DataPointDao dataPointDao;
    @Inject
    private transient WatchListDao watchListDao;
    @Inject
    private transient RuntimeManager runtimeManager;
    @Inject 
    private Localizer localizer;


    @RequestMapping(value = "/rest/watchLists", params = "id", method = RequestMethod.GET)
    public JsonWatchList getWatchList(int id) {
        LOG.severe("CALLED: getWatchList " + id);
        final JsonWatchList result = new JsonWatchList(watchListDao.getWatchList(id), dataPointDao, runtimeManager, localizer);
        for (JsonWatchListPoint jwp : result) {
            LOG.severe("JWP: "+ jwp.getCanonicalName());
        }
        return result;
    }


}
