package io.stardog.starwizard.services.parameter;

import java.util.Map;
import java.util.Optional;

public interface ParameterService {
    Map<String,String> getParameters(Iterable<String> params);
    Optional<String> optParameter(String name);

    default String getParameter(String name) {
        return optParameter(name)
                .orElseThrow(() -> new IllegalArgumentException("Unable to get required parameter: " + name));
    }
}
