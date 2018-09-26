package io.stardog.starwizard.services.stripe.exceptions;

public class UncheckedStripeException extends RuntimeException {
    public UncheckedStripeException(Throwable cause) {
        super(cause);
    }
}
