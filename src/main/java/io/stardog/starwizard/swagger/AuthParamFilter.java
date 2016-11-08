package io.stardog.starwizard.swagger;

import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.model.ApiDescription;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;

// from: https://www.reonomy.com/augmenting-dropwizard-with-swagger/
public class AuthParamFilter implements SwaggerSpecFilter {
    @Override
    public boolean isOperationAllowed(
            Operation operation,
            ApiDescription api,
            Map<String, List<String>> params,
            Map<String, String> cookies,
            Map<String, List<String>> headers) {
        return true;
    }

    @Override
    public boolean isParamAllowed(
            Parameter parameter,
            Operation operation,
            ApiDescription api,
            Map<String, List<String>> params,
            Map<String, String> cookies, Map<String,
            List<String>> headers) {
        String access = parameter.getAccess();
        if (access != null && access.equals("internal")) return false;
        return true;
    }

    @Override
    public boolean isPropertyAllowed(
            Model model,
            Property property,
            String propertyName,
            Map<String, List<String>> params,
            Map<String, String> cookies,
            Map<String, List<String>> headers) {
        return true;
    }
}