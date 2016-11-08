package io.stardog.starwizard.exceptionmappers;

import com.google.common.collect.ImmutableMap;
import io.stardog.starwizard.exceptions.ValidationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException e) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ImmutableMap.of("error", e.toString()))
                .build();
    }
}
