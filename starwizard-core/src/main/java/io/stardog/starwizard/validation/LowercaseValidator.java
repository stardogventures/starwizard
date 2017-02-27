package io.stardog.starwizard.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * This JSR303 ConstraintValidator implementation forces a field to be lowercase.
 */
public class LowercaseValidator implements ConstraintValidator<Lowercase, String> {
    @Override
    public void initialize(Lowercase lowercase) {
        // no initialization needed
    }

    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
        if (object == null) {
            return true;
        }
        return object.equals(object.toLowerCase());
    }
}
