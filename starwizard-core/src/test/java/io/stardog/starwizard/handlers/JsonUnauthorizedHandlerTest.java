package io.stardog.starwizard.handlers;

import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import static org.junit.Assert.*;

public class JsonUnauthorizedHandlerTest {
    @Test
    public void buildResponse() throws Exception {
        JsonUnauthorizedHandler handler = new JsonUnauthorizedHandler();
        Response response = handler.buildResponse("Basic", "TestRealm");
        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getMediaType().toString());
        assertEquals("Credentials are required to access this resource.", ((Map<String,Object>)response.getEntity()).get("error"));
    }

}
