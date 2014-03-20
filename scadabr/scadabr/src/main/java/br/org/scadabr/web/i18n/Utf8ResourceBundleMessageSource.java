package br.org.scadabr.web.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Utf8ResourceBundleMessageSource extends org.springframework.context.support.ResourceBundleMessageSource {

    @Override
    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        return Utf8ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
    }
}
