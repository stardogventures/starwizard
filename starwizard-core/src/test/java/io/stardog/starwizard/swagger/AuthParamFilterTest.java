package io.stardog.starwizard.swagger;

import io.swagger.models.parameters.Parameter;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AuthParamFilterTest {
    @Test
    public void isOperationAllowed() throws Exception {
        AuthParamFilter filter = new AuthParamFilter();
        assertTrue(filter.isOperationAllowed(null, null, null, null, null));
    }

    @Test
    public void isParamAllowed() throws Exception {
        AuthParamFilter filter = new AuthParamFilter();
        Parameter parameter = mock(Parameter.class);

        when(parameter.getAccess()).thenReturn("internal");
        assertFalse(filter.isParamAllowed(parameter, null, null, null, null, null));

        when(parameter.getAccess()).thenReturn(null);
        assertTrue(filter.isParamAllowed(parameter, null, null, null, null, null));
    }

    @Test
    public void isPropertyAllowed() throws Exception {
        AuthParamFilter filter = new AuthParamFilter();
        assertTrue(filter.isPropertyAllowed(null, null, null, null, null, null));
    }

}
