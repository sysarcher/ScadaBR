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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.dao.DataSourceDao;

import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.view.View;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.DataPointAccess;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.mango.web.dwr.beans.DataExportDefinition;
import com.serotonin.mango.web.dwr.beans.TestingUtility;
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

    //
    // Session data. The user object is stored in session, and some other
    // session-based information is cached here
    // for convenience.
    //
    private transient View view;
    private transient WatchList watchList;
    private transient DataPointVO editPoint;
    private transient DataSourceVO<?> editDataSource;

    private transient TestingUtility testingUtility;
    private transient Map<String, byte[]> reportImageData;
    private transient PublisherVO<? extends PublishedPointVO> editPublisher;
    private transient boolean muted = false;
    private transient DataExportDefinition dataExportDefinition;
    private final transient Map<String, Object> attributes = new HashMap<>();

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

    //
    // Testing utility management
    public <T extends TestingUtility> T getTestingUtility(Class<T> requiredClass) {
        TestingUtility tu = testingUtility;

        if (tu != null) {
            try {
                return requiredClass.cast(tu);
            } catch (ClassCastException e) {
                tu.cancel();
                testingUtility = null;
            }
        }
        return null;
    }

    public void setTestingUtility(TestingUtility testingUtility) {
        TestingUtility tu = this.testingUtility;
        if (tu != null) {
            tu.cancel();
        }
        this.testingUtility = testingUtility;
    }

    public void cancelTestingUtility() {
        setTestingUtility(null);
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

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public WatchList getWatchList() {
        return watchList;
    }

    public void setWatchList(WatchList watchList) {
        this.watchList = watchList;
    }

    public DataPointVO getEditPoint() {
        return editPoint;
    }

    public void setEditPoint(DataPointVO editPoint) {
        this.editPoint = editPoint;
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

    public DataSourceVO<?> getEditDataSource() {
        return editDataSource;
    }

    public void setEditDataSource(DataSourceVO<?> editDataSource) {
        this.editDataSource = editDataSource;
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

    public Map<String, byte[]> getReportImageData() {
        return reportImageData;
    }

    public void setReportImageData(Map<String, byte[]> reportImageData) {
        this.reportImageData = reportImageData;
    }

    public PublisherVO<? extends PublishedPointVO> getEditPublisher() {
        return editPublisher;
    }

    public void setEditPublisher(
            PublisherVO<? extends PublishedPointVO> editPublisher) {
        this.editPublisher = editPublisher;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
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

    public DataExportDefinition getDataExportDefinition() {
        return dataExportDefinition;
    }

    public void setDataExportDefinition(
            DataExportDefinition dataExportDefinition) {
        this.dataExportDefinition = dataExportDefinition;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
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
