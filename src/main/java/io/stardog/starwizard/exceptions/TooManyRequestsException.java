package io.stardog.starwizard.exceptions;

import javax.ws.rs.ClientErrorException;

/**
 * This provides an exception to throw for HTTP code 429 "Too Many Requests",
 * as documented in RFC 6585: https://tools.ietf.org/html/rfc6585#section-4
 *
 * This exception is usually used for rate-limiting.
 */
public class TooManyRequestsException extends ClientErrorException {
    public TooManyRequestsException(String message) {
        super(message, 429);
    }
}
