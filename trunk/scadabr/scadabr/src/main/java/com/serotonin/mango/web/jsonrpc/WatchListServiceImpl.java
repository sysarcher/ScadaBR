package com.serotonin.mango.web.jsonrpc;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.web.UserSessionContextBean;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Named
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WatchListServiceImpl implements WatchListService {

    private static final Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private WatchListDao watchListDao;
    
    @Inject
    private Localizer localizer;
    @Inject
    private UserSessionContextBean userSessionContextBean;

    @Override
    public JsonWatchList getWatchList(int watchlistId) {
        return new JsonWatchList(watchListDao.getWatchList(watchlistId));
    }


}
