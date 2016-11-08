package io.stardog.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import io.stardog.email.interfaces.EmailRawSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class SesEmailer extends TemplateEmailer implements EmailRawSender {
    private final AmazonSimpleEmailServiceClient client;
    private final static Logger LOGGER = LoggerFactory.getLogger(SesEmailer.class);

    @Inject
    public SesEmailer(AmazonSimpleEmailServiceClient client) {
        super();
        this.client = client;
    }

    static String toRfcFormat(String toEmail, String toName) {
        if (toName == null) {
            return toEmail;
        } else {
            return toName + " <" + toEmail + ">";
        }
    }

    public String sendEmail(String toEmail, String toName, String fromEmail, String fromName,
                            String subject, String contentHtml, String contentText) {

        Destination destination = new Destination()
                .withToAddresses(new String[]{toRfcFormat(toEmail, toName)});

        Content subjectContent = new Content().withData(subject);

        Body body = new Body();
        if (contentHtml != null) {
            body = body.withHtml(new Content().withData(contentHtml));
        }
        if (contentText != null) {
            body = body.withText(new Content().withData(contentText));
        }
        Message message = new Message().withSubject(subjectContent).withBody(body);

        SendEmailRequest request = new SendEmailRequest()
                .withSource(toRfcFormat(fromEmail, fromName))
                .withDestination(destination)
                .withMessage(message);

        try {
            String messageId = client.sendEmail(request).getMessageId();
            LOGGER.info("Sent SES email to " + toEmail + " with id " + messageId);
            return messageId;
        } catch (RuntimeException e) {
            LOGGER.error("Unable to send email via SES: ", e);
            throw e;
        }
    }
}
