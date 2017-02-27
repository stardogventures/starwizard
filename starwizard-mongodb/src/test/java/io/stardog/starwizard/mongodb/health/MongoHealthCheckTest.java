package io.stardog.starwizard.mongodb.health;

import com.codahale.metrics.health.HealthCheck;
import com.github.fakemongo.Fongo;
import org.junit.Test;

import static org.junit.Assert.*;

public class MongoHealthCheckTest {
    @Test
    public void testCheck() throws Exception {
        Fongo fongo = new Fongo("fake-mongo");
        MongoHealthCheck check = new MongoHealthCheck(fongo.getDatabase("fake-mongo-db"));
        assertTrue(check.check().isHealthy());
    }
}
