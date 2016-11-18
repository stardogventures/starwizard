package io.stardog.starwizard.exceptionmappers;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    private final static Logger LOGGER = Logger.getLogger(RuntimeExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException e) {
        LOGGER.warn("Encountered runtime exception", e);
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ImmutableMap.of("error", message))
                .build();
    }
}
