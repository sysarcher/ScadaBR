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
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.dao.UserDao;
import br.org.scadabr.dao.WatchListDao;
import br.org.scadabr.utils.ImplementMeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.serotonin.mango.view.ShareUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Matthew Lohbihler
 */
public class WatchList implements Iterable<DataPointVO> {

    @Configurable
    public static class WatchListValidator implements Validator {

        @Autowired
        private DataPointDao dataPointDao;
        @Autowired
        private UserDao userDao;
        @Autowired
        private WatchListDao watchListDao;

        @Override
        public boolean supports(Class<?> clazz) {
            return WatchList.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            final WatchList vo = (WatchList) target;
            if (vo.name.isEmpty()) {
                errors.rejectValue("name", "validate.required");
            } else if (vo.name.length() > 50) {
                errors.rejectValue("name", "validate.notLongerThan", new Object[]{50}, "validate.notLongerThan");
            }

            if (vo.xid.isEmpty()) {
                errors.rejectValue("xid", "validate.required");
            } else if (vo.xid.length() > 50) {
                errors.rejectValue("xid", "validate.notLongerThan", new Object[]{50}, "validate.notLongerThan");
            } else if (!watchListDao.isXidUnique(vo.xid, vo.id)) {
                errors.rejectValue("xid", "validate.xidUsed");
            }

            throw new ImplementMeException();
            /*
             for (DataPointVO dpVO : vo.pointList) {
             dpVO.validate(response);
             }
             */
        }

    }

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

    @Override
    public Iterator<DataPointVO> iterator() {
        return pointList.iterator();
    }

    @JsonIgnore
    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
    }
}
