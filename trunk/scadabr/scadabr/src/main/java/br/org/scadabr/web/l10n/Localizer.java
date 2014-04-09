package br.org.scadabr.web.l10n;

import br.org.scadabr.web.i18n.LocalizableMessage;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mbus4j.log.LogUtils;

/**
 * This class localizes I18N keys and messages with the default #ResourceBundle
 * or an given one.
 *
 */
public class Localizer {

    /**
     * the logger to use.
     */
    private final static Logger LOG = LogUtils.getCoreLogger();

    /**
     * Localize the message.
     *
     * @param i18nKey the I18N key
     * @param bundle the resource bundle to use.
     * @param args the parameter to pass.
     * @return the localizes I18n message.
     */
    public static String localizeI18nKey(String i18nKey, ResourceBundle bundle, Object... args) {
        Object[] localizedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof LocalizableMessage) {
                final LocalizableMessage localizableMessage = (LocalizableMessage) args[i];
                localizedArgs[i] = localizeMessage(localizableMessage, bundle);
            } else {
                localizedArgs[i] = args[i];
            }
        }
        try {
            return new MessageFormat(bundle.getString(i18nKey), bundle.getLocale()).format(localizedArgs);
        } catch (MissingResourceException e) {
            return logAndGetMessage(i18nKey, bundle.getLocale());
        }
    }

    /**
     * Localize the message.
     *
     * @param i18nMessage the I18N message.
     * @param bundle the bundle to use
     * @return the localized I18N message.
     */
    public static String localizeMessage(LocalizableMessage i18nMessage, ResourceBundle bundle) {
        return localizeI18nKey(i18nMessage.getI18nKey(), bundle, ((LocalizableMessage) i18nMessage).getArgs());
    }

    /**
     * Log a comment and generate a pseudo translated message instead of
     * throwing an exception. THis happens if the key is not found in the given
     * locale and no default message can be found.
     *
     * @param i18nKey the missing I18N key.
     * @param locale the locale.
     * @return the pseudo translated key.
     */
    private static String logAndGetMessage(String i18nKey, Locale locale) {
        LOG.log(Level.SEVERE, "Localizer found unknown I18N key {0} for locale: {2}.", new Object[]{i18nKey, locale});
        return String.format("!>>>%s:%s<<<!", locale, i18nKey);
    }

    //TODO set TimeZone ???
    public static String localizeTimeStamp(long ts, boolean hideDateOfToday, Locale locale) {
        if (hideDateOfToday && (System.currentTimeMillis() - ts) < 86400000) {
            return DateFormat.getTimeInstance(DateFormat.DEFAULT, locale).format(new Date(ts));
        } else {
            return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale).format(new Date(ts));
        }
    }

    public static String localizeDate(Locale locale, long ts) {
        return DateFormat.getDateInstance(DateFormat.DEFAULT, locale).format(new Date(ts));
    }

    public static String localizeTime(Locale locale, long ts) {
        return DateFormat.getTimeInstance(DateFormat.DEFAULT, locale).format(new Date(ts));
    }

}
