/*
 Mango - Open Source M2M - http://mango.serotoninsoftware.com
 Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
 @author Matthew Lohbihler
    
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.web.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import br.org.scadabr.db.IntValuePair;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.permission.Permissions;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.web.UserSessionContextBean;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/watch_list.shtm")
@Scope("request")
public class WatchListController {
    

    @Inject
    private UserSessionContextBean userSessionContextBean;
    @Inject
    private WatchListDao watchListDao;
    @Inject
    private UserDao userDao;
    @Inject
    private Localizer localizer;
    
    public static final String KEY_WATCHLISTS = "watchLists";
    public static final String KEY_SELECTED_WATCHLIST = "selectedWatchList";

    @RequestMapping(method = RequestMethod.GET)
    public String initializeForm(ModelMap model) {
        createModel(model);
        return "watchList";
    }

    protected void createModel(ModelMap modelMap) {
        User user = userSessionContextBean.getUser();

        // The user's permissions may have changed since the last session, so make sure the watch lists are correct.
        List<WatchList> watchLists = watchListDao.getWatchLists(user.getId());

        if (watchLists.isEmpty()) {
            // Add a default watch list if none exist.
            WatchList watchList = new WatchList();
            watchList.setName(localizer.localizeI18nKey("common.newName"));
            watchLists.add(watchListDao.createNewWatchList(watchList, user.getId()));
        }

        int selected = user.getSelectedWatchList();
        boolean found = false;

        List<IntValuePair> watchListNames = new ArrayList<>(watchLists.size());
        for (WatchList watchList : watchLists) {
            if (watchList.getId() == selected) {
                found = true;
            }

            if (watchList.getUserAccess(user) == ShareUser.ACCESS_OWNER) {
                // If this is the owner, check that the user still has access to the points. If not, remove the
                // unauthorized points, resave, and continue.
                boolean changed = false;
                List<DataPointVO> list = watchList.getPointList();
                List<DataPointVO> copy = new ArrayList<>(list);
                for (DataPointVO point : copy) {
                    if (point == null || !Permissions.hasDataPointReadPermission(user, point)) {
                        list.remove(point);
                        changed = true;
                    }
                }

                if (changed) {
                    watchListDao.saveWatchList(watchList);
                }
            }

            watchListNames.add(new IntValuePair(watchList.getId(), watchList.getName()));
        }

        if (!found) {
            // The user's default watch list was not found. It was either deleted or unshared from them. Find a new one.
            // The list will always contain at least one, so just use the id of the first in the list.
            selected = watchLists.get(0).getId();
            user.setSelectedWatchList(selected);
            watchListDao.saveSelectedWatchList(user.getId(), selected);
        }

        modelMap.put(KEY_WATCHLISTS, watchListNames);
        modelMap.put(KEY_SELECTED_WATCHLIST, selected);
        modelMap.put("NEW_ID", Common.NEW_ID);
    }
    
    
    public static class JsonWatchList {

        private JsonWatchList(WatchList watchList) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    
        public JsonWatchList setSelectedWatchList(int watchListId) {
        User user = userSessionContextBean.getUser();

        WatchList watchList = watchListDao.getWatchList(watchListId);
        Permissions.ensureWatchListPermission(user, watchList);
        prepareWatchList(watchList, user);

        watchListDao.saveSelectedWatchList(user.getId(), watchList.getId());
        user.setSelectedWatchList(watchListId);

        JsonWatchList data = getWatchListData(user, watchList);
		// Set the watchlist in the user object after getting the data since it
        // may take a while, and the long poll
        // updates will all be missed in the meantime.
        user.setWatchList(watchList);

        return data;
    }
        
    private JsonWatchList getWatchListData(User user, WatchList watchList) {
        JsonWatchList data = new JsonWatchList(watchList);

        List<DataPointVO> points = watchList.getPointList();
        List<Integer> pointIds = new ArrayList<>(points.size());
        for (DataPointVO point : points) {
            if (Permissions.hasDataPointReadPermission(user, point)) {
                pointIds.add(point.getId());
            }
        }

//        data.put("points", pointIds);
//        data.put("users", watchList.getWatchListUsers());
//        data.put("access", watchList.getUserAccess(user));

        return data;
    }
    private void prepareWatchList(WatchList watchList, User user) {
        int access = watchList.getUserAccess(user);
        User owner = userDao.getUser(watchList.getUserId());
        for (DataPointVO point : watchList.getPointList()) {
            updateSetPermission(point, access, owner);
        }
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

	//

}
