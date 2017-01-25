package io.stardog.starwizard.exceptionmappers;

import com.google.common.collect.ImmutableMap;
import io.stardog.starwizard.exceptions.OauthException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class OauthExceptionMapper implements ExceptionMapper<OauthException> {
    @Override
    public Response toResponse(OauthException e) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ImmutableMap.of("error", e.getError(), "error_description", e.getErrorDescription()))
                .build();
    }
}
