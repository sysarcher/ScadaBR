package br.org.scadabr.web.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;

public class ResourceBundleMessageSource extends org.springframework.context.support.ResourceBundleMessageSource {

    @Override
    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        return ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
    }
}
