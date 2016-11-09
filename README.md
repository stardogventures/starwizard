# starwizard

Supporting classes for Dropwizard REST APIs

Dropwizard is awesome, and I've found it incredibly useful for building simple, scalable REST APIs. But while Dropwizard is opinionated, there are some things that it (appropriately) doesn't include in the box, which I've found I tend to re-use over and over when building Dropwizard APIs. Here they are in one place.

To use Starwizard, add the following to your project's POM file:

```
<dependency>
    <groupId>io.stardog</groupId>
    <artifactId>starwizard</artifactId>
    <version>0.1.0</version>
</dependency>
```

And:

```
<repositories>
    <repository>
        <id>stardog-maven</id>
        <name>stardog-maven</name>
        <url>http://maven.stardog.io</url>
    </repository>
</repositories>
```

Then add any of the following to your Dropwizard app's main `Application` class's `run` method -- for the below I am assuming your `Environment` var is named `env`:

# JSON exception mappers

If you include the below, then all error responses will return as JSON instead of HTML. Unanticipated RuntimeExceptions will be logged as errors.

```
env.jersey().register(new WebExceptionMapper());
env.jersey().register(new JsonMappingExceptionMapper());
env.jersey().register(new IllegalStateExceptionMapper());
env.jersey().register(new ValidationExceptionMapper());
env.jersey().register(new RuntimeExceptionMapper());
```

# http to https redirect for Amazon ELB

If you are running Dropwizard behind ELB and want to redirect all HTTP requests to HTTPS:

```
env.getApplicationContext().addFilter(new FilterHolder(new LbHttpsRedirectFilter()), "/*",
    EnumSet.of(DispatcherType.REQUEST));
```

I often add an `forceHttps` setting to the yml file to determine whether to do this, so I have some flexibility for non-HTTPS dev deployments.

# swagger 'internal' designation

If you are using Swagger, the following will allow you to designate parameters as "internal", meaning they won't be included in the Swagger definitions. This is primarily useful for @Auth parameters.

```
FilterFactory.setFilter(new AuthParamFilter());
```

Example of how to define a parameter as internal:
```
@ApiParam(access = "internal") @Auth User user,
```
