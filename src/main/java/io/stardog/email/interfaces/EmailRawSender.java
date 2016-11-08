package io.stardog.email.interfaces;

/**
 * An EmailRawSender is capable of sending raw emails.
 */
public interface EmailRawSender {
    String sendEmail(String toEmail, String toName, String fromEmail, String fromName,
                     String subject, String contentHtml, String contentText);
}
