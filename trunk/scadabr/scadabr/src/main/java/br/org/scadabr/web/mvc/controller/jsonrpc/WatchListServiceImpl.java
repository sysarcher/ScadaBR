package br.org.scadabr.web.mvc.controller.jsonrpc;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.UserSessionContextBean;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

//TODO Watchlist scope???

@Named
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WatchListServiceImpl implements WatchListService, Serializable {

    private static final Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private transient DataPointDao dataPointDao;
    @Inject
    private transient WatchListDao watchListDao;
    @Inject
    private transient RuntimeManager runtimeManager;
    @Inject
    private transient Localizer localizer;
    @Inject
    private transient UserDao userDao;
    @Inject
    private transient UserSessionContextBean userSessionContextBean;

    @Override
    public JsonWatchList getWatchList(int watchlistId) {
        return new JsonWatchList(watchListDao.getWatchList(watchlistId), dataPointDao, runtimeManager, localizer);
    }

    @Override
    public JsonWatchList getSelectedWatchlist() {
        return new JsonWatchList(watchListDao.getWatchList(userSessionContextBean.getUser().getSelectedWatchList()), dataPointDao, runtimeManager, localizer );
    }

    @Override
    public JsonWatchList addPointToWatchlist(int watchlistId, int index, int dataPointId) {
        LOG.warning("ENTER addPointToWatchlist");
        final User user = userSessionContextBean.getUser();
        DataPointVO point = dataPointDao.getDataPoint(dataPointId);
        if (point == null) {
            return null;
        }
        WatchList watchList = watchListDao.getWatchList(watchlistId);

        // Check permissions.
        Permissions.ensureDataPointReadPermission(user, point);
        Permissions.ensureWatchListEditPermission(user, watchList);

        // Add it to the watch list.
        watchList.getPointList().add(index, point);
        watchListDao.saveWatchList(watchList);
        updateSetPermission(point, watchList.getUserAccess(user), userDao.getUser(watchList.getUserId()));
        LOG.warning("ENTER addPointToWatchlist " + watchListDao.getWatchList(watchlistId).getName());
        return new JsonWatchList(watchListDao.getWatchList(watchlistId), dataPointDao, runtimeManager, localizer); 
    }
    
    private void updateSetPermission(DataPointVO point, int access, User owner) {
        // Point isn't settable
        if (!point.getPointLocator().isSettable()) {
            return;
        }

        // Read-only access
        if (access != ShareUser.ACCESS_OWNER && access != ShareUser.ACCESS_SET) {
            return;
        }

        // Watch list owner doesn't have set permission
        if (!Permissions.hasDataPointSetPermission(owner, point)) {
            return;
        }

        // All good.
        point.setSettable(true);
    }

}
