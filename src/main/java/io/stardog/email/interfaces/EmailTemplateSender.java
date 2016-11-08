package io.stardog.email.interfaces;

import io.stardog.email.data.EmailTemplate;

import java.util.Map;

/**
 * An EmailTemplateSender is capable of sending emails by the name of that template.
 *  Any specifics involving adding templates, etc, are specific to the name of that implementation.
 */
public interface EmailTemplateSender {
    public String sendTemplate(String templateName, String toEmail, String toName, Map<String, Object> vars);
    public void addTemplate(EmailTemplate emailTemplate);
    public void putGlobalVar(String key, Object val);
}
