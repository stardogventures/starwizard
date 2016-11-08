package io.stardog.email;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;

/**
 * A version of an emailer that does nothing. Useful for tests or local use.
 */
public class NonEmailer extends TemplateEmailer {
    @Override
    public String sendTemplate(String templateName, String toEmail, String toName, Map<String, Object> vars) {
        return RandomStringUtils.randomAlphanumeric(32);
    }

    @Override
    public String sendEmail(String toEmail, String toName, String fromEmail, String fromName, String subject, String contentHtml, String contentText) {
        return RandomStringUtils.randomAlphanumeric(32);
    }
}
