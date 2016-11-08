package io.stardog.starwizard.exceptions;

import javax.ws.rs.ClientErrorException;

public class TooManyRequestsException extends ClientErrorException {
    public TooManyRequestsException(String message) {
        super(message, 429);
    }
}
