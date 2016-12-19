package io.stardog.starwizard.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This JSR303 annotation forces the annotated field to be lowercase.
 */
@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = LowercaseValidator.class)
@Documented
public @interface Lowercase {

    String message() default "must be lowercase";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}