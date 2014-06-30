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
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.view.chart.ChartRenderer;
import com.serotonin.mango.view.chart.ChartType;
import com.serotonin.mango.web.UserSessionContextBean;
import java.util.Date;
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


    @RequestMapping(value = "/rest/watchlists", params = "id", method = RequestMethod.GET)
    public @ResponseBody JsonWatchList getWatchList(int id) {
        LOG.severe("CALLED: getWatchList " + id);
        final JsonWatchList result = new JsonWatchList(watchListDao.getWatchList(id), dataPointDao, runtimeManager, localizer);
        return result;
    }


}
