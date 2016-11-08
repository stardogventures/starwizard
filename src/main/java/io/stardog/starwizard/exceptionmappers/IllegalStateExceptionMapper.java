package io.stardog.starwizard.exceptionmappers;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
    @Override
    public Response toResponse(IllegalStateException e) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ImmutableMap.of("error", e.getMessage()))
                .build();
    }
}
