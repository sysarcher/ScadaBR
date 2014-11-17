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
public class ContentPaneTag extends TagSupport {
    
    protected DataDojoProps dataDojoProps = new DataDojoProps();
    
    
    public ContentPaneTag() {
        dataDojoProps.put("parseOnLoad", false);
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
            out.append(" data-dojo-type=\"dojox/layout/ContentPane\" ");
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
    
    public void setSplitter(boolean splitter) {
        dataDojoProps.put("splitter", splitter);
    }

}
