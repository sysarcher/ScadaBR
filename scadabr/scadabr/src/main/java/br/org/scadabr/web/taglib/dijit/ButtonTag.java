/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dijit;

import static br.org.scadabr.web.taglib.Functions.printAttribute;
import br.org.scadabr.web.taglib.dojo.DataDojoProps;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 *
 * @author aploese
 */
public class ButtonTag extends RequestContextAwareTag {

    private String name;
    private String value;
    private String i18nLabel;
    private String i18nTitle;
    private String type;
    private DataDojoProps dataDojoProps = new DataDojoProps();
    private boolean disabled;

    @Override
    public void release() {
        super.release();
        id = null;
        value = null;
        i18nLabel = null;
        i18nTitle = null;
        type = null;
        dataDojoProps = null;
        disabled = false;
    }

    @Override
    public int doStartTagInternal() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<button");
            printAttribute(out, "id", id);
            printAttribute(out, "type", type);
            out.append(" data-dojo-type=\"dijit/form/Button\" ");
            // make Button as wide as image...
            if (i18nLabel == null && !dataDojoProps.containsKey("showLabel") && dataDojoProps.containsKey("iconClass")) {
                dataDojoProps.put("showLabel", false);
            }
            dataDojoProps.print(out);
            printAttribute(out, "name", name);
            if (i18nTitle != null) {
                printAttribute(out, "title", getRequestContext().getMessage(i18nTitle));
            }
            if (disabled) {
                printAttribute(out, "disabled", "true");
            }
            out.print(">");
            if (i18nLabel != null) {
                out.print(getRequestContext().getMessage(i18nLabel));
            }
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            out.print("</button>");
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
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

    /**
     * @param iconClass the iconClass to set
     */
    public void setIconClass(String iconClass) {
        dataDojoProps.put("iconClass", iconClass);
    }

    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
