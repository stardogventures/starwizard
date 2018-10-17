# starwizard

### Supporting classes for Dropwizard REST APIs

Dropwizard is awesome, and I've found it incredibly useful for building simple, scalable REST APIs. But while Dropwizard is opinionated, there are some things that it (appropriately) doesn't include in the box, which I've found I tend to re-use over and over when building Dropwizard APIs.

* I usually run Dropwizard APIs on AWS behind an Elastic Load Balancer. ELB doesn't have an automatic way to force HTTPS, so I always need an easy way to redirect HTTP requests to HTTPS.
* I want all my error responses to be JSON. These days Dropwizard defaults to JSON errors for most exceptions, which is great! But 401s when using auth still returns a text/plain response, which is not so great. A simple `JsonUnauthorizedHandler` class forces a JSON response.
* I like using Java 8 time classes, but out of the box Jersey doesn't allow the use of `LocalDate` and `Instant` as query or path parameters. A simple `JavaTimeParamConverterProvider` supports these.
* I nearly always need to write an Oauth2 provider, which needs responses and exceptions compliant with the Oauth2 spec. Resuable classes help here.

Here are all these reusables in one place. Hope someone else finds it useful!

Some of these classes are not of my invention:
  * `AuthParamFilter` is from this  excellent blog post by [Pablo Meier](https://github.com/pablo-meier): https://www.reonomy.com/augmenting-dropwizard-with-swagger/, based on this StackOverflow answer by [Özkan Can](http://stackoverflow.com/users/2494590/%C3%96zkan-can) http://stackoverflow.com/questions/21911166/how-can-i-set-swagger-to-ignore-suspended-asyncresponse-in-asynchronous-jax-rs [Note: This is no longer necessary since in newer versions of Swagger you can now use `hidden = true` in @ApiParam]
  * `JsonUnauthorizedHandler` is from this equally excellent blog post by [Nick Babcock](https://github.com/nickbabcock): https://nbsoftsolutions.com/blog/writing-a-dropwizard-json-app
  
# Module: starwizard-mongodb

New in version 0.1.3 - a handful of useful classes specifically aimed at using MongoDB.

Read the [starwizard-mongodb README](/starwizard-mongodb/README.md) for more information.

# Module: starwizard-services

New in version 0.2.0 - reusable supporting service classes, including Slack, Stripe, AWS SSM, and Google Geocoding APIs.

Read the [starwizard-services README](/starwizard-services/README.md) for more information.

# Installation: starwizard-core

To use Starwizard Core, add the following to your project's POM file:

```
<properties>
    <starwizard.version>0.2.0</starwizard.version>
    ...
</properties>

<dependencies>
  <dependency>
    <groupId>io.stardog.starwizard</groupId>
    <artifactId>starwizard-core</artifactId>
    <version>${starwizard.version}</version>
  </dependency>
  ...
</dependencies>
```

To add MongoDB:

```
  <dependency>
    <groupId>io.stardog.starwizard</groupId>
    <artifactId>starwizard-mongodb</artifactId>
    <version>${starwizard.version}</version>
  </dependency>
```

To add Services:

```
  <dependency>
    <groupId>io.stardog.starwizard</groupId>
    <artifactId>starwizard-services</artifactId>
    <version>${starwizard.version}</version>
  </dependency>
```

# Features and Usage

## HTTP to HTTPS redirect for Amazon ELB

If you are running Dropwizard behind ELB and want to redirect all HTTP requests to HTTPS:

```java
env.getApplicationContext().addFilter(new FilterHolder(new LbHttpsRedirectFilter()), "/*",
    EnumSet.of(DispatcherType.REQUEST));
```

I often add an `forceHttps` setting to the yml file to conditionally determine whether to do this, so I have some flexibility for non-HTTPS dev deployments.

## JSON 401 responses

To force JSON responses when Unauthorized, call `setUnauthorizedHandler(new JsonUnauthorizedHandler())` on your `AuthFilterBuilder`. Here is an example for Oauth:

```java
env.jersey().register(new AuthDynamicFeature(
        new OAuthCredentialAuthFilter.Builder<UserAuth>()
                .setAuthenticator(authenticator)
                .setPrefix("Bearer")
                .setUnauthorizedHandler(new JsonUnauthorizedHandler())
                .buildAuthFilter()));
```

## Support for Java 8 java.time Instant and LocalDate parameters

It's often useful to use `Instant`s and `LocalDate`s as query string or path parameters.

For `LocalDate`, the format must be standard ISO-8601 (YYYY-MM-DD). For `Instant`, you are permitted to either use millisecond timestamp integers, or ISO-8601 format.

```
env.jersey().register(new JavaTimeParamConverterProvider());
```

# Other stuff included

## Oauth related classes: OauthException, OauthExceptionMapper, AccessTokenResponse

When writing an Oauth2-compliant resource for an authorization server, return `AccessTokenResponse` for a successfully issued token, and throw `OauthException` on a failure.

Register the exception mapper to get the error returned in the manner required by the Oauth spec:

```java
env.jersey().register(new OauthExceptionMapper());
```

## TooManyRequestsException

HTTP status code 429 (Too Many Requests) is a useful error for rate-limiting schemes. This provides a JAX-RS exception for this code.

## @Lowercase Hibernate validation annotation

Sometimes (especially for email addresses) it's important to enforce lowercase. This provides a simple annotation to do that:

```java
@Lowercase
public abstract String getEmail();
```

# Other Dropwizard setup recommendations

None of the below involves starwizard -- these are all part of standard Dropwizard. But I usually do the below every time, so here they are in one place:

## Allow logging of full request/response

When you're debugging a nasty problem, there's nothing quite like examining full requests and responses (including headers). The below configures Jersey's `LoggingFeature` to log in DEBUG mode:

```java
env.jersey().register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                                         Level.FINE, LoggingFeature.Verbosity.PAYLOAD_ANY, 100000));
```

If you're using default INFO log levels, requests/responses will not log. To temporarily turn on logging for your service, use the builtin Dropwizard `log-level` task:

```shell
curl -X POST "http://localhost:8081/tasks/log-level?logger=org.glassfish.jersey.logging.LoggingFeature&level=DEBUG"
```

Set it back after you're finished debugging:

```shell
curl -X POST "http://localhost:8081/tasks/log-level?logger=org.glassfish.jersey.logging.LoggingFeature&level=INFO"
```

## Configure the ObjectMapper

I always use Java 8 so I want support for Optionals and JavaTime date/time classes. I like my Instants to render as ISO-8601 strings. I also prefer to be generous about ignoring unknown properties on input, and do not like exposing null fields.

```java
ObjectMapper objectMapper = env.getObjectMapper();
objectMapper.registerModule(new Jdk8Module());
objectMapper.registerModule(new JavaTimeModule());
objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
```

## Set up CORS to allow cross domain requests

I nearly always host the API on a different domain than my UI, so the below is handy:

```java
final FilterRegistration.Dynamic cors = env.servlets().addFilter("CORS", CrossOriginFilter.class);
cors.setInitParameter("allowedOrigins", "https://web.example.com"); // or replace with your domain names
cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,Authorization");
cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,PATCH,HEAD");
cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
```

## Turn on multipart-form support (must include dropwizard-forms)

Only if you are dealing with file uploads. I almost always wind up needing file uploads in my APIs.

```java
env.jersey().register(MultiPartFeature.class);
```
