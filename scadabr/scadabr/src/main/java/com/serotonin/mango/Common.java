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
package com.serotonin.mango;

import gnu.io.CommPortIdentifier;

import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.db.KeyValuePair;
import br.org.scadabr.l10n.AbstractLocalizer;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.util.CommPortConfigException;
import com.serotonin.mango.view.View;
import com.serotonin.mango.view.custom.CustomView;
import com.serotonin.mango.vo.CommPortProxy;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.web.ContextWrapper;
import br.org.scadabr.monitor.MonitoredValues;
import br.org.scadabr.timer.CronTimerPool;
import br.org.scadabr.timer.cron.DataSourceCronTask;
import br.org.scadabr.timer.cron.DataSourceRunnable;
import br.org.scadabr.timer.cron.EventCronTask;
import br.org.scadabr.timer.cron.EventRunnable;
import br.org.scadabr.timer.cron.SystemCronTask;
import br.org.scadabr.timer.cron.SystemRunnable;
import br.org.scadabr.util.StringUtils;
import br.org.scadabr.utils.ImplementMeException;
import java.util.MissingResourceException;
import javax.inject.Inject;
import javax.inject.Named;

@Deprecated // Convert to singleton bean
@Named
public class Common {
    
    @Inject
    private SystemSettingsDao systemSettingsDao;

    @Deprecated
    private static final String SESSION_USER = "sessionUser";
    private static final String ANON_VIEW_KEY = "anonymousViews";
    private static final String CUSTOM_VIEW_KEY = "customView";

    public static final String UTF8 = "UTF-8";
    public static final Charset UTF8_CS = Charset.forName(UTF8);

    public static final int NEW_ID = -1;
    @Deprecated
    public static ContextWrapper ctx;

    //TODO inject this
    private static final ResourceBundle env = ResourceBundle.getBundle("env");

    // This is initialized
    public static CronTimerPool<DataSourceCronTask, DataSourceRunnable> dataSourcePool;
    public static CronTimerPool<SystemCronTask, SystemRunnable> systemCronPool;
    public static CronTimerPool<EventCronTask, EventRunnable> eventCronPool;

    @Deprecated // move to own bean ??? And what is monitored ???
    public static final MonitoredValues MONITORED_VALUES = new MonitoredValues();

    /*
     * Updating the Mango version: - Create a DBUpdate subclass for the old
     * version number. This may not do anything in particular to the schema, but
     * is still required to update the system settings so that the database has
     * the correct version.
     */
    @Deprecated // Use ScadaBrVersion bean
    public static final String getVersion() {
        return "0.9.1";
    }

    @Deprecated
    public interface ContextKeys {

        @Deprecated
        String IMAGE_SETS = "IMAGE_SETS";
        @Deprecated
        String DYNAMIC_IMAGES = "DYNAMIC_IMAGES";
        @Deprecated
        String SCHEDULER = "SCHEDULER";
        String FREEMARKER_CONFIG = "FREEMARKER_CONFIG";
        String BACKGROUND_PROCESSING = "BACKGROUND_PROCESSING";
        String HTTP_RECEIVER_MULTICASTER = "HTTP_RECEIVER_MULTICASTER";
        String DOCUMENTATION_MANIFEST = "DOCUMENTATION_MANIFEST";
        String DATA_POINTS_NAME_ID_MAPPING = "DATAPOINTS_NAME_ID_MAPPING";
    }

    public interface GroveServlets {

        String VERSION_CHECK = "versionCheckComm";
        String MANGO_LOG = "mangoLog";
    }

    //
    // Session user
    @Deprecated
    public static User getUser() {
        WebContext webContext = WebContextFactory.get();
        if (webContext == null) {
            // If there is no web context, check if there is a background
            // context
            BackgroundContext backgroundContext = BackgroundContext.get();
            if (backgroundContext == null) {
                return null;
            }
            return backgroundContext.getUser();
        }
        return getUser(webContext.getHttpServletRequest());
    }

    @Deprecated
    public static User getUser(HttpServletRequest request) {
        if (true) {
            throw new RuntimeException("REMOVED >>USE @Inject UserSessionContextBean");
        }
        // Check first to see if the user object is in the request.
        User user = (User) request.getAttribute(SESSION_USER);
        if (user != null) {
            return user;
        }

        // If not, get it from the session.
        user = (User) request.getSession().getAttribute(SESSION_USER);

        if (user != null) // Add the user to the request. This prevents race conditions in
        // which long-ish lasting requests have the
        // user object swiped from them by a quicker (logout) request.
        {
            request.setAttribute(SESSION_USER, user);
        }

        return user;
    }

    @Deprecated
    public static void setUser(HttpServletRequest request, User user) {
        if (true) {
            throw new RuntimeException("REMOVED USE: @Inject UserSessionContextBean");
        }
        request.getSession().setAttribute(SESSION_USER, user);
    }

    //
    // Background process description. Used for audit logs when the system
    // automatically makes changes to data, such as
    // safe mode disabling stuff.
    public static String getBackgroundProcessDescription() {
        BackgroundContext backgroundContext = BackgroundContext.get();
        if (backgroundContext == null) {
            return null;
        }
        return backgroundContext.getProcessDescriptionKey();
    }

    //
    // Anonymous views
    public static View getAnonymousView(int id) {
        return getAnonymousView(
                WebContextFactory.get().getHttpServletRequest(), id);
    }

    public static View getAnonymousView(HttpServletRequest request, int id) {
        List<View> views = getAnonymousViews(request);
        if (views == null) {
            return null;
        }
        for (View view : views) {
            if (view.getId() == id) {
                return view;
            }
        }
        return null;
    }

    public static void addAnonymousView(HttpServletRequest request, View view) {
        List<View> views = getAnonymousViews(request);
        if (views == null) {
            views = new ArrayList<>();
            request.getSession().setAttribute(ANON_VIEW_KEY, views);
        }
        // Remove the view if it already exists.
        for (int i = views.size() - 1; i >= 0; i--) {
            if (views.get(i).getId() == view.getId()) {
                views.remove(i);
            }
        }
        views.add(view);
    }

    @SuppressWarnings("unchecked")
    private static List<View> getAnonymousViews(HttpServletRequest request) {
        return (List<View>) request.getSession().getAttribute(ANON_VIEW_KEY);
    }

    //
    // Custom views
    public static CustomView getCustomView() {
        return getCustomView(WebContextFactory.get().getHttpServletRequest());
    }

    public static CustomView getCustomView(HttpServletRequest request) {
        return (CustomView) request.getSession().getAttribute(CUSTOM_VIEW_KEY);
    }

    public static void setCustomView(HttpServletRequest request, CustomView view) {
        request.getSession().setAttribute(CUSTOM_VIEW_KEY, view);
    }

    //
    // Environment profile
    public static ResourceBundle getEnvironmentProfile() {
        return env;
    }

    public static boolean getEnvironmentBoolean(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(env.getString(key));
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static int getEnvironmentInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(env.getString(key));
        } catch (MissingResourceException | NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getEnvironmentString(String key, String defaultValue) {
        try {
            return env.getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static String getGroveUrl(String servlet) {
        final String grove = getEnvironmentString("grove.url",
                "http://mango.serotoninsoftware.com/servlet");
        return grove + "/" + servlet;
    }

    public static String getDocPath() {
        return ctx.getServletContext().getRealPath("/WEB-INF/dox") + "/";
    }

    private static String lazyFiledataPath = null;

    public String getFiledataPath() {
        if (lazyFiledataPath == null) {
            String name = systemSettingsDao.getValue(SystemSettingsDao.FILEDATA_PATH);
            if (name.startsWith("~")) {
                name = ctx.getServletContext().getRealPath(name.substring(1));
            }

            File file = new File(name);
            if (!file.exists()) {
                file.mkdirs();
            }

            lazyFiledataPath = name;
        }
        return lazyFiledataPath;
    }

    //
    // Misc
    @Deprecated
    public static List<CommPortProxy> getCommPorts()
            throws CommPortConfigException {
        try {
            List<CommPortProxy> ports = new LinkedList<>();
            Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
            CommPortIdentifier cpid;
            while (portEnum.hasMoreElements()) {
                cpid = (CommPortIdentifier) portEnum.nextElement();
                if (cpid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    ports.add(new CommPortProxy(cpid));
                }
            }
            return ports;
        } catch (UnsatisfiedLinkError e) {
            throw new CommPortConfigException(e.getMessage());
        } catch (NoClassDefFoundError e) {
            throw new CommPortConfigException(
                    "Comm configuration error. Check that rxtx DLL or libraries have been correctly installed.");
        }
    }

    public synchronized static String encrypt(String plaintext) {
        try {
            String alg = getEnvironmentString("security.hashAlgorithm", "SHA");
            if ("NONE".equals(alg)) {
                return plaintext;
            }

            MessageDigest md = MessageDigest.getInstance(alg);
            if (md == null) {
                throw new ShouldNeverHappenException(
                        "MessageDigest algorithm "
                        + alg
                        + " not found. Set the 'security.hashAlgorithm' property in env.properties appropriately. "
                        + "Use 'NONE' for no hashing.");
            }
            md.update(plaintext.getBytes(UTF8_CS));
            byte raw[] = md.digest();
            String hash = new String(Base64.encodeBase64(raw));
            return hash;
        } catch (NoSuchAlgorithmException e) {
            // Should never happen, so just wrap in a runtime exception and
            // rethrow
            throw new ShouldNeverHappenException(e);
        }
    }

    //
    // HttpClient
    public HttpClient getHttpClient() {
        return getHttpClient(30000); // 30 seconds.
    }

    public HttpClient getHttpClient(int timeout) {
        HttpConnectionManagerParams managerParams = new HttpConnectionManagerParams();
        managerParams.setConnectionTimeout(timeout);
        managerParams.setSoTimeout(timeout);

        HttpClientParams params = new HttpClientParams();
        params.setSoTimeout(timeout);

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().setParams(managerParams);
        client.setParams(params);

        if (systemSettingsDao
                .getBooleanValue(SystemSettingsDao.HTTP_CLIENT_USE_PROXY)) {
            String proxyHost = systemSettingsDao
                    .getValue(SystemSettingsDao.HTTP_CLIENT_PROXY_SERVER);
            int proxyPort = systemSettingsDao
                    .getIntValue(SystemSettingsDao.HTTP_CLIENT_PROXY_PORT);

            // Set up the proxy configuration.
            client.getHostConfiguration().setProxy(proxyHost, proxyPort);

            // Set up the proxy credentials. All realms and hosts.
            client.getState()
                    .setProxyCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(
                                    systemSettingsDao
                                    .getValue(
                                            SystemSettingsDao.HTTP_CLIENT_PROXY_USERNAME,
                                            ""),
                                    systemSettingsDao
                                    .getValue(
                                            SystemSettingsDao.HTTP_CLIENT_PROXY_PASSWORD,
                                            "")));
        }

        return client;
    }

    @Deprecated
    public static String getMessage(String key) {
throw new ImplementMeException();
// ensureI18n();
//        return AbstractLocalizer.localizeI18nKey(key, systemBundle);
    }

    @Deprecated // Use per user settings ...
    public static ResourceBundle getBundle() {
throw new ImplementMeException();
    //    ensureI18n();
//        return systemBundle;
    }

    @Deprecated
    public static String getMessage(String key, Object... args) {
        String pattern = getMessage(key);
        return MessageFormat.format(pattern, args);
    }

    @Deprecated
    private static Locale findLocale(String language) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getLanguage().equals(language)) {
                return locale;
            }
        }
        return null;
    }

    public static List<KeyValuePair> getLanguages() {
        List<KeyValuePair> languages = new ArrayList<>();
        ResourceBundle i18n = ResourceBundle.getBundle("i18n");
        for (String key : i18n.keySet()) {
            languages.add(new KeyValuePair(key, i18n.getString(key)));
        }
        return languages;
    }

    public static String generateXid(String prefix) {
        return prefix + StringUtils.generateRandomString(6, "0123456789");
    }

}
