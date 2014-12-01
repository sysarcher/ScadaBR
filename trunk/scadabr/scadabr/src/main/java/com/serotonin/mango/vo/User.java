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
import java.util.List;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.dao.DataSourceDao;

import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.vo.permission.DataPointAccess;
import com.serotonin.mango.vo.permission.Permissions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class User implements Serializable, SetPointSource {

    @Configurable
    public static class UserValidator implements Validator {

        @Autowired
        private DataPointDao dataPointDao;
        @Autowired
        private DataSourceDao dataSourceDao;

        @Override
        public boolean supports(Class<?> clazz) {
            return User.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            final User vo = (User) target;
            if (vo.username.isEmpty()) {
                errors.rejectValue("username", "validate.required");
            }
            if (vo.email.isEmpty()) {
                errors.rejectValue("email", "validate.required");
            }
            if (vo.id == ScadaBrConstants.NEW_ID && vo.password.isEmpty()) {
                errors.rejectValue("password", "validate.required");
            }

            // Check field lengths
            if (vo.username.length() > 40) {
                errors.rejectValue("username", "validate.notLongerThan", new Object[]{40}, "validate.notLongerThan");
            }
            if (vo.email.length() > 255) {
                errors.rejectValue("email", "validate.notLongerThan", new Object[]{255}, "validate.notLongerThan");
            }
            if (vo.phone.length() > 40) {
                errors.rejectValue("phone", "validate.notLongerThan", new Object[]{40}, "validate.notLongerThan");
            }
        }

    }

    private int id = ScadaBrConstants.NEW_ID;

    private String username;

    private String password;

    private String email;

    private String phone;

    private boolean admin;

    private boolean disabled;
    private List<Integer> dataSourcePermissions;
    private List<DataPointAccess> dataPointPermissions;
    private int selectedWatchList;

    private String homeUrl;
    private long lastLogin;
    private AlarmLevel receiveAlarmEmails;

    private boolean receiveOwnAuditEvents;

    /**
     * Used for various display purposes.
     *
     * @return
     */
    public String getDescription() {
        return username + " (" + id + ")";
    }

    public boolean isFirstLogin() {
        return lastLogin == 0;
    }

    //
    // /
    // / SetPointSource implementation
    // /
    //
    @Override
    public int getSetPointSourceId() {
        return id;
    }

    @Override
    public int getSetPointSourceType() {
        return SetPointSource.Types.USER;
    }

    @Override
    public void raiseRecursionFailureEvent() {
        throw new ShouldNeverHappenException("");
    }

    // Convenience method for JSPs
    public boolean isDataSourcePermission() {
        return Permissions.hasDataSourcePermission(this);
    }

    // Properties
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<Integer> getDataSourcePermissions() {
        return dataSourcePermissions;
    }

    public void setDataSourcePermissions(List<Integer> dataSourcePermissions) {
        this.dataSourcePermissions = dataSourcePermissions;
    }

    public List<DataPointAccess> getDataPointPermissions() {
        return dataPointPermissions;
    }

    public void setDataPointPermissions(
            List<DataPointAccess> dataPointPermissions) {
        this.dataPointPermissions = dataPointPermissions;
    }

    public int getSelectedWatchList() {
        return selectedWatchList;
    }

    public void setSelectedWatchList(int selectedWatchList) {
        this.selectedWatchList = selectedWatchList;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public AlarmLevel getReceiveAlarmEmails() {
        return receiveAlarmEmails;
    }

    public void setReceiveAlarmEmails(AlarmLevel receiveAlarmEmails) {
        this.receiveAlarmEmails = receiveAlarmEmails;
    }

    public boolean isReceiveOwnAuditEvents() {
        return receiveOwnAuditEvents;
    }

    public void setReceiveOwnAuditEvents(boolean receiveOwnAuditEvents) {
        this.receiveOwnAuditEvents = receiveOwnAuditEvents;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return id == other.id;
    }

    public boolean isReceiveAlarmEmails() {
        return receiveAlarmEmails != null;
    }

    @JsonIgnore
    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
    }

}
