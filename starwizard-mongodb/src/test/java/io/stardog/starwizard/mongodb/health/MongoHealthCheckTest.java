package io.stardog.starwizard.mongodb.health;

import com.codahale.metrics.health.HealthCheck;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MongoHealthCheckTest {
    @Test
    public void testCheck() throws Exception {
        Fongo fongo = new Fongo("fake-mongo");
        MongoHealthCheck check = new MongoHealthCheck(fongo.getDatabase("fake-mongo-db"));
        assertTrue(check.check().isHealthy());
    }

    @Test
    public void testException() throws Exception {
        MongoDatabase db = mock(MongoDatabase.class);
        when(db.runCommand(new Document("ping", 1)))
                .thenThrow(new MongoTimeoutException("Timeout"));

        MongoHealthCheck check = new MongoHealthCheck(db);
        HealthCheck.Result result = check.check();

        assertFalse(result.isHealthy());
        assertEquals("Timeout", result.getMessage());
    }

    @Test
    public void testWrongResponse() throws Exception {
        MongoDatabase db = mock(MongoDatabase.class);
        when(db.runCommand(new Document("ping", 1)))
                .thenReturn(new Document("ok", false));

        MongoHealthCheck check = new MongoHealthCheck(db);
        HealthCheck.Result result = check.check();

        assertFalse(result.isHealthy());
        assertEquals("Unexpected ping response: Document{{ok=false}}", result.getMessage());
    }
}
