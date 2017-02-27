package io.stardog.starwizard.mongodb.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * A Dropwizard health check that makes sure you can connect to and get a response from your MongoDB instance,
 * via the MongoDB "ping" command.
 */
public class MongoHealthCheck extends HealthCheck {
    private final MongoDatabase db;
    private final static Document PING = new Document("ping", 1);
    private final static Document OK = new Document("ok", 1.0);
    private final static Logger LOGGER = LoggerFactory.getLogger(MongoHealthCheck.class);

    @Inject
    public MongoHealthCheck(MongoDatabase db) {
        this.db = db;
    }

    @Override
    protected Result check() throws Exception {
        try {
            Document result = db.runCommand(PING);
            if (!OK.equals(result)) {
                LOGGER.warn("Unexpected ping response: " + result.toString());
                return Result.unhealthy("Unexpected ping response: " + result.toString());
            }
            return Result.healthy();
        } catch (Exception e) {
            LOGGER.warn("Failed health check", e);
            return Result.unhealthy(e.getMessage());
        }
    }
}
