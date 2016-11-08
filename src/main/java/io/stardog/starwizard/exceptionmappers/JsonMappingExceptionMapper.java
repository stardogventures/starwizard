package io.stardog.starwizard.exceptionmappers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {
    @Override
    public Response toResponse(JsonMappingException e) {
        if (e.getCause() == null) {
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(ImmutableMap.of("error", e.getMessage()))
                    .build();
        } else {
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(ImmutableMap.of("error", e.getCause().getMessage()))
                    .build();
        }
    }
}
