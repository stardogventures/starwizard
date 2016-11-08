package io.stardog.starwizard.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = LowercaseValidator.class)
@Documented
public @interface Lowercase {

    String message() default "must be lowercase";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class LowercaseValidator implements ConstraintValidator<Lowercase, String> {
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
