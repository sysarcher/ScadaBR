/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.taglib.dijit;

import br.org.scadabr.web.i18n.I18NUtils;
import br.org.scadabr.web.l10n.Localizer;
import static br.org.scadabr.web.taglib.Functions.printAttribute;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author aploese
 */
public class TextBoxTag extends TagSupport {

    private String name;
    private String value;
    private String i18nLabel;
    private String i18nTitle;
    private String type;
    
        @Override
    public void release() {
        super.release();
        id = null;
        value = null;
        i18nLabel = null;
        i18nTitle = null;
        type = null;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<input");
            printAttribute(out, "id", id);
            printAttribute(out, "type", type);
            out.append(" data-dojo-type=\"dijit/form/TextBox\" ");
            printAttribute(out, "name", name);
            printAttribute(out, "label", Localizer.localizeI18nKey(i18nLabel, I18NUtils.getBundle(pageContext)) + ":");
            printAttribute(out, "title", Localizer.localizeI18nKey(i18nTitle != null ? i18nTitle : i18nLabel, I18NUtils.getBundle(pageContext)));
            printAttribute(out, "value", value);
            out.println("/>");
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @param i18nLabel the i18nLabel to set
     */
    public void setI18nLabel(String i18nLabel) {
        this.i18nLabel = i18nLabel;
    }

    /**
     * @param i18nTitle the i18nTitle to set
     */
    public void setI18nTitle(String i18nTitle) {
        this.i18nTitle = i18nTitle;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

}
