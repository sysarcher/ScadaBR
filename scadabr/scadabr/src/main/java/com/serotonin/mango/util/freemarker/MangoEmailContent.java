package com.serotonin.mango.util.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;

import com.serotonin.mango.Common;
import com.serotonin.web.email.TemplateEmailContent;

public class MangoEmailContent extends TemplateEmailContent {

    public enum EmailContentType {
        BOTH,
        HTML,
        TEXT;
    }

    private MangoEmailContent(String templateName, Object model, String encoding, EmailContentType type) throws TemplateException,
            IOException {
        super(EmailContentType.HTML.equals(type) ? null : getTemplate(templateName, false), EmailContentType.TEXT.equals(type) ? null
                : getTemplate(templateName, true), model, encoding);
    }

    private static Template getTemplate(String name, boolean html) throws IOException {
        if (html) {
            name = "html/" + name + ".ftl";
        } else {
            name = "text/" + name + ".ftl";
        }

        return Common.ctx.getFreemarkerConfig().getTemplate(name);
    }
}
