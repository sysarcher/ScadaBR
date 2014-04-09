package com.serotonin.mango.web.email;

import br.org.scadabr.web.email.EmailContent;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.SystemSettingsDao;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MangoEmailContent extends EmailContent {

    public static final int CONTENT_TYPE_BOTH = 0;
    public static final int CONTENT_TYPE_HTML = 1;
    public static final int CONTENT_TYPE_TEXT = 2;

    private final String defaultSubject;
    private final SubjectDirective subjectDirective;

    public MangoEmailContent(String templateName, Map<String, Object> model, ResourceBundle bundle,
            String defaultSubject, String encoding) throws TemplateException, IOException {
        super(null, null, encoding);

        int type = SystemSettingsDao.getIntValue(SystemSettingsDao.EMAIL_CONTENT_TYPE);

        this.defaultSubject = defaultSubject;
        this.subjectDirective = new SubjectDirective(bundle);

        model.put("fmt", new MessageFormatDirective(bundle));
        model.put("subject", subjectDirective);

        if (type == CONTENT_TYPE_HTML || type == CONTENT_TYPE_BOTH) {
            htmlContent =  processHtmlTemplate(getHTMLTemplate(templateName), model);
        }

        if (type == CONTENT_TYPE_TEXT || type == CONTENT_TYPE_BOTH) {
            plainContent = processPlainTemplate(getPlainTemplate(templateName), model);
        }
    }

    public String getSubject() {
        String subject = subjectDirective.getSubject();
        if (subject == null) {
            return defaultSubject;
        }
        return subject;
    }

    private Template getPlainTemplate(String name) throws IOException {
        return Common.ctx.getFreemarkerConfig().getTemplate(String.format("text/%s.ftl", name));
    }

    private Template getHTMLTemplate(String name) throws IOException {
        return Common.ctx.getFreemarkerConfig().getTemplate(String.format("html/%s.ftl", name));
    }
}
