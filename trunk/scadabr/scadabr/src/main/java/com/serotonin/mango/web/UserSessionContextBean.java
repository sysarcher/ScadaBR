/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.web;

import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.event.type.SystemEventSource;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.vo.User;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author aploese
 */
@Named
@Scope("session")
public class UserSessionContextBean implements Serializable {

    @Inject
    private transient EventManager eventManager;

    private User user;
    private Locale locale = Locale.getDefault();
    private TimeZone timeZone = TimeZone.getDefault();
    private transient DateFormat dateFormat;
    private transient DateFormat timeFormat;
    private transient DateFormat dateTimeFormat;
    private transient ResourceBundle bundle;

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void loginUser(User user) {
        this.user = user;
        eventManager.raiseEvent(new SystemEventType(
                SystemEventSource.USER_LOGIN, user.getId()), System
                .currentTimeMillis(), true, new LocalizableMessageImpl(
                        "event.login", user.getUsername()));
    }

    /**
     * @param user the user to set
     */
    public void logoutUser(User user) {
        this.user = null;
        eventManager.returnToNormal(new SystemEventType(
                SystemEventSource.USER_LOGIN, user.getId()), System
                .currentTimeMillis());
        user.cancelTestingUtility();
    }

    
    @PreDestroy
    public void preDestroy() {
        //forcibly ending
        if (user != null) {
            loginUser(user);
        }
    }
    
    public String getUsername() {
        return user != null ? user.getUsername() : "anonymous";
    }
    
    public String getUserHomeUrl() {
        return user != null ? user.getHomeUrl(): "";
    }
    
    public boolean isLoggedIn() {
        return user != null;
    }
    
    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the dateFormat
     */
    public DateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        }
        return dateFormat;
    }

    /**
     * @param dateFormat the dateFormat to set
     */
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * @return the timeFormat
     */
    public DateFormat getTimeFormat() {
        if (timeFormat == null) {
            timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
        }
        return timeFormat;
    }

    /**
     * @param timeFormat the timeFormat to set
     */
    public void setTimeFormat(DateFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    /**
     * @return the dateTimeFormat
     */
    public DateFormat getDateTimeFormat() {
        if (dateTimeFormat == null) {
            dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        }
        return dateTimeFormat;
    }

    /**
     * @param dateTimeFormat the dateTimeFormat to set
     */
    public void setDateTimeFormat(DateFormat dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * @return the bundle
     */
    public ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("messages", locale);
        }
        return bundle;
    }

}