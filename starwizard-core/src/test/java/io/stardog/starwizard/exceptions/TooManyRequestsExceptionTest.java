package io.stardog.starwizard.exceptions;

import org.junit.Test;

import static org.junit.Assert.*;

public class TooManyRequestsExceptionTest {
    @Test
    public void testExceptionCreation() throws Exception {
        TooManyRequestsException exception = new TooManyRequestsException("Too many requests!");
        assertEquals(429, exception.getResponse().getStatus());
    }
}
