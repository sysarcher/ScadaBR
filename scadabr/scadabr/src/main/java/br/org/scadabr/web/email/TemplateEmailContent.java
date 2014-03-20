/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.email;

import br.org.scadabr.ImplementMeException;
import freemarker.template.Template;
import java.util.Map;

/**
 *
 * @author aploese
 */
public class TemplateEmailContent extends EmailContent {

    public TemplateEmailContent(Template t1, Template t2, Object o, String s) {
        super(s, s, s);
        throw new ImplementMeException();
    }

    public TemplateEmailContent(String s) {
        super(s, s, s);
        throw new ImplementMeException();
    }

    protected void setHtmlTemplate(Template template, Map<String, Object> model) {
        throw new ImplementMeException();
    }

    protected void setPlainTemplate(Template template, Map<String, Object> model) {
        throw new ImplementMeException();
    }

}
