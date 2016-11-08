package io.stardog.starwizard.exceptions;

import io.stardog.starwizard.models.AbstractModel;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class ValidationException extends RuntimeException {
    private final Set<ConstraintViolation<AbstractModel>> violations;

    public ValidationException(Set<ConstraintViolation<AbstractModel>> violations) {
        this.violations = violations;
    }

    public String toString() {
        StringBuilder errors = new StringBuilder();
        errors.append("Unable to validate: ");
        boolean first = true;
        for (ConstraintViolation violation : violations) {
            if (!first) {
                errors.append("; ");
            } else {
                first = false;
            }
            errors.append(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return errors.toString();
    }

    public String getMessage() {
        return toString();
    }
}
