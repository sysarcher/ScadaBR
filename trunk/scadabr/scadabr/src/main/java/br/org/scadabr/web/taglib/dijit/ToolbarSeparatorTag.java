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
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author aploese
 */
public class ToolbarSeparatorTag extends TagSupport {

        @Override
    public void release() {
        super.release();
        id = null;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<span");
            printAttribute(out, "id", id);
            out.append(" data-dojo-type=\"dijit/ToolbarSeparator\" ");
            out.print(">");
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            out.print("</span>");
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
    }

}
