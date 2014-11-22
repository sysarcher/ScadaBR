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
package com.serotonin.mango.vo;

import br.org.scadabr.ScadaBrConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.view.ShareUser;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Matthew Lohbihler
 */

@Configurable
public class WatchList implements Iterable<DataPointVO> {
    
    @Autowired
    private DataPointDao dataPointDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private WatchListDao watchListDao;

    public static final String XID_PREFIX = "WL_";

    private int id = ScadaBrConstants.NEW_ID;
    private String xid;
    private int userId;
    
    private String name;
    private final List<DataPointVO> pointList = new CopyOnWriteArrayList<>();
    private List<ShareUser> watchListUsers = new ArrayList<>();

    public int getUserAccess(User user) {
        if (user.getId() == userId) {
            return ShareUser.ACCESS_OWNER;
        }

        for (ShareUser wlu : watchListUsers) {
            if (wlu.getUserId() == user.getId()) {
                return wlu.getAccessType();
            }
        }
        return ShareUser.ACCESS_NONE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }
    }

    public List<DataPointVO> getPointList() {
        return pointList;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<ShareUser> getWatchListUsers() {
        return watchListUsers;
    }

    public void setWatchListUsers(List<ShareUser> watchListUsers) {
        this.watchListUsers = watchListUsers;
    }

    public void validate(DwrResponseI18n response) {
        if (name.isEmpty()) {
            response.addContextual("name", "validate.required");
        } else if (name.length() >  50) {
            response.addContextual("name", "validate.notLongerThan", 50);
        }

        if (xid.isEmpty()) {
            response.addContextual("xid", "validate.required");
        } else if (xid.length() >  50) {
            response.addContextual("xid", "validate.notLongerThan", 50);
        } else if (!watchListDao.isXidUnique(xid, id)) {
            response.addContextual("xid", "validate.xidUsed");
        }

        for (DataPointVO dpVO : pointList) {
            dpVO.validate(response);
        }
    }

    @Override
    public Iterator<DataPointVO> iterator() {
        return pointList.iterator();
    }

    @JsonIgnore
    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
    }
}
