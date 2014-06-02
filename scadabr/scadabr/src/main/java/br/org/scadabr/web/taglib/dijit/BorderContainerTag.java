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
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author aploese
 */
public class BorderContainerTag extends TagSupport {

    private DataDojoProps dataDojoProps = new DataDojoProps();
    
    
    protected BorderContainerTag(String design) {
        dataDojoProps.put("design", design);
    }

    @Override
    public void release() {
        super.release();
        id = null;
        dataDojoProps = null;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            out.print("<div");
            printAttribute(out, "id", id);
            out.append(" data-dojo-type=\"dijit/layout/BorderContainer\" ");
            dataDojoProps.print(out); 
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
    
    public void setGutters(boolean gutters) {
        dataDojoProps.put("gutters", gutters);
    }
    
    public void setLiveSplitters(boolean liveSplitters) {
        dataDojoProps.put("liveSplitters", liveSplitters);
    }

}
