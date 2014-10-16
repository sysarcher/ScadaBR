/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dojox;

import static br.org.scadabr.web.taglib.Functions.printAttribute;
import br.org.scadabr.web.taglib.dojo.DataDojoProps;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author aploese
 */
public class TableContainerTag extends TagSupport {

    private DataDojoProps dataDojoProps = new DataDojoProps();
    private String style; 
    
    @Override
    public void release() {
        super.release();
        id = null;
        dataDojoProps = null;
        style = null;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<div");
            printAttribute(out, "id", id);
            out.append(" data-dojo-type=\"dojox/layout/TableContainer\" ");
            dataDojoProps.print(out);
            printAttribute(out, "style", style);
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
            out.print("</div>");
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
    }

    
    public void setCols(int cols) {
        dataDojoProps.put("cols", cols);
    }

    /**
     * @param style the style to set
     */
    public void setStyle(String style) {
        this.style = style;
    }
}
