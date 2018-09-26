package io.stardog.starwizard.services.parameter;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Singleton
public interface ParameterService {
    Map<String,String> getParameters(Iterable<String> params);
    Optional<String> optParameter(String name);

    default String getParameter(String name) {
        return optParameter(name)
                .orElseThrow(() -> new IllegalArgumentException("Unable to get required parameter: " + name));
    }
}
