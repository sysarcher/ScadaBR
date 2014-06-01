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
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author aploese
 */
public class ButtonTag extends TagSupport {

    private String name;
    private String value;
    private String i18nLabel;
    private String i18nTitle;
    private String type;
    private Map<String, Object> dataDojoProps;

    @Override
    public void release() {
        super.release();
        id = null;
        value = null;
        i18nLabel = null;
        i18nTitle = null;
        type = null;
        dataDojoProps = null;
    }

    @Override
    public int doStartTag() throws JspException {
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
            if (dataDojoProps != null) {
                boolean firstProp = true;
                out.append(" data-dojo-props=\"");
                for (String prop : dataDojoProps.keySet()) {
                    if (firstProp) {
                        firstProp = false;
                    } else {
                        out.append(", ");
                    }
                    out.append(prop);
                    out.append(": ");
                    final Object propValue = dataDojoProps.get(prop);
                    if (propValue instanceof Boolean) {
                        out.append(propValue.toString());
                    } else if (propValue instanceof Number) {
                        out.append(propValue.toString());
                    } else {
                        out.append('\'');
                        out.append(propValue.toString());
                        out.append('\'');
                    }
                }
                out.append('\"');

            }
            printAttribute(out, "name", name);
            if (i18nTitle != null) {
                printAttribute(out, "title", Localizer.localizeI18nKey(i18nTitle, I18NUtils.getBundle(pageContext)));
            }
            out.print(">");
            if (i18nLabel != null) {
                out.print(Localizer.localizeI18nKey(i18nLabel, I18NUtils.getBundle(pageContext)));
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
        addToDataDojoProps("iconClass", iconClass);
    }

    private void addToDataDojoProps(String propKey, String propValue) {
        if (dataDojoProps == null) {
            dataDojoProps = new HashMap<>();
        }
        dataDojoProps.put(propKey, propValue);
    }

}
