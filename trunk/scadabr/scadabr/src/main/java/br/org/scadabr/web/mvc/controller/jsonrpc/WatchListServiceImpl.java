package br.org.scadabr.web.mvc.controller.jsonrpc;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.web.UserSessionContextBean;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Named
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WatchListServiceImpl implements WatchListService, Serializable {

    private static final Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private transient WatchListDao watchListDao;
    @Inject
    private transient UserSessionContextBean userSessionContextBean;

    @Override
    public JsonWatchList getWatchList(int watchlistId) {
        return new JsonWatchList(watchListDao.getWatchList(watchlistId));
    }

    @Override
    public JsonWatchList getSelectedWatchlist() {
        return new JsonWatchList(watchListDao.getWatchList(userSessionContextBean.getUser().getSelectedWatchList()));
    }


}
