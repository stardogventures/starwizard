package io.stardog.starwizard.exceptionmappers;

import org.junit.Test;

import javax.ws.rs.core.Response;
import static org.junit.Assert.assertEquals;

public class RuntimeExceptionMapperTest {
    @Test
    public void testToResponse() throws Exception {
        RuntimeExceptionMapper mapper = new RuntimeExceptionMapper();

        Response nullResponse = mapper.toResponse(new NullPointerException());
        assertEquals(500, nullResponse.getStatus());
    }
}