/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dijit;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import static br.org.scadabr.web.taglib.Functions.printAttribute;
import java.util.Map;
import javax.el.ValueExpression;

/**
 *
 * @author aploese
 */
public class SelectFromMapTag extends TagSupport {

    private String value;
    private Map<String, String> map;

    public void setMap(Object map) {
        if (map instanceof ValueExpression) {
            ValueExpression deferredExpression = (ValueExpression) map;
            this.map = (Map<String, String>) deferredExpression.getValue(pageContext.getELContext());
        } else {
            this.map = (Map<String, String>) map;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void release() {
        super.release();
        id = null;
        value = null;
        map = null;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<select");
            printAttribute(out, "id", id);
            out.print(" data-dojo-type=\"dijit/form/Select\" ");
            out.print(">");
            for (String key : map.keySet()) {
//                out.newLine();
                out.print(" <option");
                printAttribute(out, "value", key);
                if (key.equals(value)) {
                    out.print(" selected=\"selected\"");
                }
                out.print('>');
                out.print(map.get(key));
                out.print("</option>");
            }
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
    }

    @Override
    public int doEndTag()
            throws JspException {
        try {
            JspWriter out = pageContext.getOut();
//            out.newLine();
            out.print("</select>");
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
    }

    public String getValue() {
        return value;
    }
}
