package io.stardog.starwizard.exceptionmappers;

import io.stardog.starwizard.exceptions.OauthException;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OauthExceptionMapperTest {

    @Test
    public void testToResponse() throws Exception {
        OauthExceptionMapper mapper = new OauthExceptionMapper();
        OauthException exception = new OauthException("invalid_client", "Invalid credentials");
        Response response = mapper.toResponse(exception);
        assertEquals(400, response.getStatus());
        assertEquals("invalid_client", ((Map)response.getEntity()).get("error"));
        assertEquals("Invalid credentials", ((Map)response.getEntity()).get("error_description"));
    }
}