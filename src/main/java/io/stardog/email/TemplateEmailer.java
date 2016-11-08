package io.stardog.email;

import com.github.mustachejava.Mustache;
import io.stardog.email.data.EmailTemplate;
import io.stardog.email.interfaces.EmailRawSender;
import io.stardog.email.interfaces.EmailTemplateSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class TemplateEmailer implements EmailTemplateSender, EmailRawSender {
    private final Map<String,Object> globalVars = new HashMap<>();
    private final Map<String,EmailTemplate> templates = new HashMap<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplateEmailer.class);

    public void putGlobalVar(String key, Object val) {
        globalVars.put(key, val);
    }

    public void addTemplate(EmailTemplate template) {
        templates.put(template.getTemplateName(), template);
    }

    @Override
    public String sendTemplate(String templateName, String toEmail, String toName, Map<String, Object> vars) {
        LOGGER.info("Called send with " + toEmail + " / " + templateName + " / " + vars);

        EmailTemplate et = templates.get(templateName);
        if (et == null) {
            LOGGER.error("Template not found: " + templateName);
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        Map<String,Object> scope = new HashMap<>();
        scope.putAll(globalVars);
        scope.putAll(vars);
        scope.put("email", toEmail);
        scope.put("name", toName);

        String fromName = et.getFromName();
        String fromEmail = et.getFromEmail();

        String subject = null;
        Mustache subjectMustache = et.getSubject();
        if (subjectMustache != null) {
            StringWriter sw = new StringWriter();
            subjectMustache.execute(sw, scope);
            subject = sw.toString();
        }

        String contentHtml = null;
        Mustache contentHtmlMustache = et.getContentHtml();
        if (contentHtmlMustache != null) {
            StringWriter sw = new StringWriter();
            contentHtmlMustache.execute(sw, scope);
            contentHtml = sw.toString();
        }

        String contentText = null;
        Mustache contentTextMustache = et.getContentText();
        if (contentTextMustache != null) {
            StringWriter sw = new StringWriter();
            contentTextMustache.execute(sw, scope);
            contentText = sw.toString();
        }

        return sendEmail(toEmail, toName, fromEmail, fromName, subject, contentHtml, contentText);
    }
}
