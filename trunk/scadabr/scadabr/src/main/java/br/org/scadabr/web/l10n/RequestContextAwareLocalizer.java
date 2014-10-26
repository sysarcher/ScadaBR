/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.l10n;

import br.org.scadabr.l10n.Localizer;
import br.org.scadabr.web.i18n.LocaleResolver;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.MessageSource;
import java.text.DateFormat;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author aploese
 */
@Named
public class RequestContextAwareLocalizer implements Localizer {

    @Inject
    private HttpServletRequest request;
    @Inject
    private MessageSource messageSource;
    @Inject
    private LocaleResolver localeResolver;
    
    public RequestContextAwareLocalizer() {
    }
    
    @Override
    public String localizeTimeStamp(long ts, boolean hideDateOfToday) {
        DateFormat df;
        if (hideDateOfToday && (System.currentTimeMillis() - ts) < 86400000) {
            df = DateFormat.getTimeInstance(DateFormat.DEFAULT, localeResolver.resolveLocale(request));
        } else {
            df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, localeResolver.resolveLocale(request));
        }
        df.setTimeZone(localeResolver.resolverTimeZone(request));
        return df.format(new Date(ts));
    }

    @Override
    public String localizeDate(long ts) {
        final DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, localeResolver.resolveLocale(request));
        df.setTimeZone(localeResolver.resolverTimeZone(request));
        return df.format(new Date(ts));
    }

    @Override
    public String localizeTime(long ts) {
        final DateFormat df = DateFormat.getTimeInstance(DateFormat.DEFAULT, localeResolver.resolveLocale(request));
        df.setTimeZone(localeResolver.resolverTimeZone(request));
        return df.format(new Date(ts));
    }

    @Override
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, localeResolver.resolveLocale(request));
    }

    @Override
    public String getMessage(LocalizableMessage localizableMessage) {
        return messageSource.getMessage(localizableMessage.getI18nKey(), localizableMessage.getArgs(), localeResolver.resolveLocale(request));
    }

}
