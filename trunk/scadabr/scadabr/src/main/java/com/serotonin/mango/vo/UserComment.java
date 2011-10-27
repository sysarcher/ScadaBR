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

import com.serotonin.web.taglib.DateFunctions;
import com.serotonin.web.taglib.Functions;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
//TODO split this class in two: EventComment and DataPointComment
public abstract class UserComment {

    // Configuration fields
    private int userId;
    private Date ts;
    private String comment;
 
    // Relational fields
    private String username;

    public UserComment() {
    }

    public UserComment(User user, String commnet) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.ts = Calendar.getInstance().getTime();
        this.comment = commnet;
    }
    
    //TODO Localization???
    public String getPrettyTime() {
        return DateFormat.getDateTimeInstance().format(ts);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
       this.comment =  Functions.truncate(comment, 1024);
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
