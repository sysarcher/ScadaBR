/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dijit;

import static br.org.scadabr.web.taglib.Functions.printAttribute;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import org.springframework.web.servlet.tags.BindTag;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 *
 * @author aploese
 */
public class TextBoxTag extends RequestContextAwareTag {

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
    public int doStartTagInternal() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<input");
            printAttribute(out, "id", id);
            printAttribute(out, "type", type);
            out.print(" data-dojo-type=\"dijit/form/TextBox\" ");
            printAttribute(out, "label", getRequestContext().getMessage(i18nLabel) + ":");
            printAttribute(out, "title", getRequestContext().getMessage(i18nTitle != null ? i18nTitle : i18nLabel));
            if (getParent() instanceof BindTag) {
                BindTag bindTag = (BindTag) getParent();
                printAttribute(out, "name", bindTag.getProperty());
                printAttribute(out, "value", bindTag.getEditor().getAsText());
            } else {
                printAttribute(out, "name", name);
                printAttribute(out, "value", value);
            }
            out.print("/>");
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
