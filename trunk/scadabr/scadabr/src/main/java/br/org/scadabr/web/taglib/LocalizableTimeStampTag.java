/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib;

import br.org.scadabr.web.i18n.I18NUtils;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author aploese
 */
public class LocalizableTimeStampTag extends TagSupport {

    public static String getFullSecondTime(long time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private long timestamp;
    private boolean hideDateOfToday;

    //TODO get proper date from ??? Usersettings???? - so currently all that is older 24 hours from now 
    @Override
    public int doEndTag() throws JspException {
        LocalizationContext lc = (LocalizationContext) Config.find(pageContext, Config.FMT_LOCALIZATION_CONTEXT);
        try {
            if (hideDateOfToday && (System.currentTimeMillis() - timestamp) < 86400000) {
                pageContext.getOut().write(DateFormat.getTimeInstance(DateFormat.DEFAULT, lc.getLocale()).format(new Date(timestamp)));
            } else {
                pageContext.getOut().write(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, lc.getLocale()).format(new Date(timestamp)));
            }
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param hideDateOfToday the hideDateOfToday to set
     */
    public void setHideDateOfToday(boolean hideDateOfToday) {
        this.hideDateOfToday = hideDateOfToday;
    }

}
