package io.stardog.starwizard.validation;

import org.junit.Test;

import static org.junit.Assert.*;

public class LowercaseValidatorTest {
    @Test
    public void initialize() throws Exception {
        LowercaseValidator validator = new LowercaseValidator();
        validator.initialize(null);
    }

    @Test
    public void isValid() throws Exception {
        LowercaseValidator validator = new LowercaseValidator();
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("lowercase@example.com", null));
        assertFalse(validator.isValid("capitalized@Example.com", null));
    }

}
