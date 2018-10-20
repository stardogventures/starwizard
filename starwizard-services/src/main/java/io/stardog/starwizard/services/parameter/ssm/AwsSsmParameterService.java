package io.stardog.starwizard.services.parameter.ssm;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.stardog.starwizard.services.parameter.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A ParameterService that returns parameters from AWS SSM Parameter Store, or environment variables as an override.
 *
 * Supports an "offline mode" for when you're running without a connection to AWS -- in offline mode, it will retrieve
 * from environment variables, but not from SSM; if an environment variable is unavailable, it will return the string
 * "OFFLINE", which is usually better than returning an error.
 *
 * By convention, env vars are UPPERCASE_UNDERSCORE and SSM params are snake_case. Typically you would prefix
 * your env vars with "APPNAME_", and prefix the SSM parameters with "/env/appname"
 *
 * For example, you might set envPrefix to "EXAMPLE_" and ssmPrefix to "/prod/example/". When you request the parameter
 * "stripe_api_key", it will check to see if there is an EXAMPLE_STRIPE_API_KEY env var; if there isn't, it'll request
 * /prod/example/stripe_api_key from SSM.
 */
@Singleton
public class AwsSsmParameterService implements ParameterService {
    private final AWSSimpleSystemsManagement ssm;
    private final String ssmPrefix;
    private final String envPrefix;
    private final boolean isOffline;
    private final static String OFFLINE_PLACEHOLDER_STRING = "OFFLINE";
    private final Logger LOGGER = LoggerFactory.getLogger(AwsSsmParameterService.class);

    private LoadingCache<String,String> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String name) {
                    return getParameter(name);
                }
            });

    @Inject
    public AwsSsmParameterService(AWSSimpleSystemsManagement ssm, @Named("ssmPrefix") String ssmPrefix,
                                  @Named("envPrefix") String envPrefix, @Named("ssmOffline") boolean isOffline) {
        this.ssm = ssm;
        this.ssmPrefix = ssmPrefix;
        this.envPrefix = envPrefix;
        this.isOffline = isOffline;
    }

    /**
     * Return an individual parameter from the cache, reducing the number of calls to SSM.
     * @param name  parameter name
     * @return  parameter value
     */
    public String getCachedParameter(String name) {
        try {
            return cache.get(name);
        } catch (ExecutionException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    /**
     * Given a number of parameters at once, reducing the number of individual calls to SSM.
     * @param params    list of parameter names to return
     * @return  map of parameter name to parameter values. If any parameters are missing, they will be excluded.
     */
    public Map<String,String> getParameters(Iterable<String> params) {
        ImmutableMap.Builder<String,String> builder = ImmutableMap.builder();
        for (String param : params) {
            getEnvParameter(param)
                    .ifPresent(v -> builder.put(param, v));
        }
        Set<String> ssmNames = Sets.difference(ImmutableSet.copyOf(params), builder.build().keySet())
                .stream().map(name -> ssmPrefix + name).collect(Collectors.toSet());
        if (!ssmNames.isEmpty()) {
            if (isOffline) {
                for (String param : ssmNames) {
                    builder.put(param, OFFLINE_PLACEHOLDER_STRING);
                }
            } else {
                List<String> ssmParams = new ArrayList<>(ssmNames);
                // SSM only allows returning 10 parameters at a time, so do in groups of 10 if needed
                for (int i=0; i < (ssmParams.size() + 9) / 10; i++) {
                    List<String> batchParams = ssmParams.subList(i*10, Math.min(ssmParams.size(), (i+1)*10));
                    GetParametersRequest request = new GetParametersRequest()
                            .withNames(batchParams)
                            .withWithDecryption(true);
                    GetParametersResult result = ssm.getParameters(request);
                    for (Parameter p : result.getParameters()) {
                        builder.put(p.getName().replaceAll(ssmPrefix, ""), p.getValue());
                    }
                    LOGGER.info("Retrieved parameters from SSM: " + batchParams);
                }
            }
        }

        return builder.build();
    }

    /**
     * Given an optional parameter name, return the parameter value, or empty if the parameter is not found.
     * @param name  parameter name
     * @return  parameter value
     */
    public Optional<String> optParameter(String name) {
        Optional<String> value = getEnvParameter(name);
        if (value.isPresent()) {
            return value;
        }
        String ssmName = ssmPrefix + name;
        GetParameterRequest request = new GetParameterRequest()
                .withName(ssmName)
                .withWithDecryption(true);
        try {
            GetParameterResult result = ssm.getParameter(request);
            LOGGER.info("Retrieved parameter from SSM: " + ssmName);
            return Optional.of(result.getParameter().getValue());
        } catch (ParameterNotFoundException e) {
            LOGGER.warn("Missing parameter: " + ssmName);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve parameter from SSM: " + ssmName);
            throw e;
        }
    }

    /**
     * Return a parameter from environment vars, or the offline placeholder if offline, or empty if not in env vars
     * @param name  parameter name
     * @return  parameter value
     */
    private Optional<String> getEnvParameter(String name) {
        if (envPrefix != null) {
            String envName = envPrefix + name.toUpperCase();
            String env = System.getenv(envName);
            if (env != null) {
                LOGGER.info("Retrieving parameter from env var: " + envName);
                return Optional.of(env);
            }
        }
        if (isOffline) {
            LOGGER.info("Offline mode, returning placeholder for: " + name);
            return Optional.of(OFFLINE_PLACEHOLDER_STRING);
        }
        return Optional.empty();
    }
}
