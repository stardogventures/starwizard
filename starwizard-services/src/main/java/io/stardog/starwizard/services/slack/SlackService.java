package io.stardog.starwizard.services.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpService;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SlackService is a simple way to send notifications to an internal or external Slack channel using the "incoming
 * webhook" feature of Slack. It is configured by default with an "internal" channel -- all calls to send() will
 * send to that internal channel.
 *
 * If you want to configure the service to not really send to Slack (useful for local use), you can set enableSlack to false.
 *
 * If you want to configure the service to send to Slack, but only ever send to an internal channel (useful
 * for dev / test environments), you can set isInternalOnly to true and optionally pass an internalPrefix.
 */
@Singleton
public class SlackService {
    private final String internalWebhookUrl;
    private final String internalChannel;
    private final String defaultUsername;
    private final String defaultIconUrl;
    private final boolean isEnabled;
    private final HttpClient httpClient;
    private final String internalPrefix;
    private final boolean isInternalOnly;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    @Inject
    public SlackService(@Named("slackWebhookUrl") String internalWebhookUrl,
                        @Named("slackUsername") String defaultUsername, @Named("slackChannel") String internalChannel,
                        @Named("slackIconUrl") String defaultIconUrl, @Named("enableSlack") boolean isEnabled,
                        @Named("slackInternalOnly") boolean isInternalOnly, @Named("envPrefix") String internalPrefix) {
        this.internalWebhookUrl = internalWebhookUrl;
        this.internalChannel = internalChannel;
        this.defaultUsername = defaultUsername;
        this.defaultIconUrl = defaultIconUrl;
        this.isEnabled = isEnabled;
        this.isInternalOnly = isInternalOnly;
        this.internalPrefix = internalPrefix;

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // set SSL timeout http://stackoverflow.com/questions/9925113/httpclient-stuck-without-any-exception
        SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(5000)
                .build();
        cm.setDefaultSocketConfig(sc);
        this.httpClient = HttpClientBuilder.create()
                .disableAutomaticRetries()
                .setConnectionManager(cm)
                .build();
    }

    public void send(String webhookUrl, String channel, String username, String iconUrl, String text, @Nullable List<Map<String,Object>> attachments) {
        try {
            // in internal-only mode, send all slack messages to the internal webhook and channel -- never send externally
            if (isInternalOnly && isEnabled) {
                channel = internalChannel;
                webhookUrl = internalWebhookUrl;
                if (internalPrefix != null && text != null) {
                    text = internalPrefix + text;
                }
            }

            Map<String, Object> message = new HashMap<>();
            message.put("username", username);
            if (channel != null) {
                message.put("channel", channel);
            }
            if (iconUrl != null) {
                message.put("icon_url", iconUrl);
            }
            message.put("text", text);
            if (attachments != null) {
                message.put("attachments", attachments);
            }
            String messageJson = OBJECT_MAPPER.writeValueAsString(message);
            if (!isEnabled) {
                LOGGER.info("Skipping actual Slack send to " + channel + " of " + messageJson);
                return;
            }

            // mask full webhook url so it doesn't show in logs
            LOGGER.info("Sending Slack message to webhook: " + webhookUrl.substring(0, 53) + "...");

            RequestConfig config = RequestConfig.custom()
                    .setConnectionRequestTimeout(10 * 1000)
                    .setConnectTimeout(10 * 1000)
                    .setSocketTimeout(10 * 1000)
                    .build();

            HttpPost post = new HttpPost(webhookUrl);
            post.setConfig(config);
            post.setHeader("Content-type", "application/json");
            post.setEntity(new StringEntity(messageJson));
            HttpResponse response = httpClient.execute(post);

            EntityUtils.consumeQuietly(response.getEntity());

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void send(String webhookUrl, String channel, String text, @Nullable List<Map<String,Object>> attachments) {
        send(webhookUrl, channel, defaultUsername, defaultIconUrl, text, attachments);
    }

    public void send(String text) {
        if (internalChannel == null || defaultUsername == null || "".equals(internalChannel) || "".equals(defaultUsername)) {
            return;
        }
        send(internalWebhookUrl, internalChannel, text, null);
    }
}