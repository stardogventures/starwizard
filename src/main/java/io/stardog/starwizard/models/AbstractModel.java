package io.stardog.starwizard.models;

import io.stardog.starwizard.exceptions.ValidationException;
import io.stardog.starwizard.validation.Required;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Set;

public class AbstractModel {
    protected final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public boolean validate() {
        Set<ConstraintViolation<AbstractModel>> violations = VALIDATOR.validate(this, Default.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
        return true;
    }

    public boolean validateRequired() {
        Set<ConstraintViolation<AbstractModel>> violations = VALIDATOR.validate(this, Required.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
        return true;
    }
}

