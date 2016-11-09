# starwizard

Supporting classes for Dropwizard REST APIs

Dropwizard is awesome, and I've found it incredibly useful for building simple, scalable REST APIs. But while Dropwizard is opinionated, there are some things that it (appropriately) doesn't include in the box, which I've found I tend to re-use over and over when building Dropwizard APIs. Here they are in one place.

Several of these classes are not of my invention:
  * `WebExceptionMapper` and `JsonMappingExceptionMapper` are from this excellent blog post by [Nick Babcock](https://github.com/nickbabcock): https://nbsoftsolutions.com/blog/writing-a-dropwizard-json-app
  * `AuthParamFilter` is from this equally excellent blog post by [Pablo Meier](https://github.com/pablo-meier): https://www.reonomy.com/augmenting-dropwizard-with-swagger/, based on this StackOverflow answer: http://stackoverflow.com/questions/21911166/how-can-i-set-swagger-to-ignore-suspended-asyncresponse-in-asynchronous-jax-rs

# Installation

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

# Features and Usage

## JSON exception mappers

If you include the below, then all error responses will return as JSON instead of HTML. Unanticipated `RuntimeException`s (e.g. `NullPointerException`s) will be logged as warnings.

Add this to your Dropwizard app's main `Application` class's `run` method (assuming your `Environment` parameter is named `env`):

```java
env.jersey().register(new WebExceptionMapper());
env.jersey().register(new JsonMappingExceptionMapper());
env.jersey().register(new RuntimeExceptionMapper());
env.jersey().register(new ValidationExceptionMapper());
```

## http to https redirect for Amazon ELB

If you are running Dropwizard behind ELB and want to redirect all HTTP requests to HTTPS:

```java
env.getApplicationContext().addFilter(new FilterHolder(new LbHttpsRedirectFilter()), "/*",
    EnumSet.of(DispatcherType.REQUEST));
```

I often add an `forceHttps` setting to the yml file to conditionally determine whether to do this, so I have some flexibility for non-HTTPS dev deployments.

## swagger 'internal' designation

If you are using Swagger, the following will allow you to designate parameters as "internal", meaning they won't be included in the Swagger definitions. This is primarily useful for @Auth parameters.

```java
FilterFactory.setFilter(new AuthParamFilter());
```

Example of how to define a parameter as internal:
```java
@ApiParam(access = "internal") @Auth User user,
```

# Other stuff included

## TooManyRequestsException

HTTP status code 429 (Too Many Requests) is a useful error for rate-limiting schemes. This provides a JAX-RS exception for this code.

## @Lowercase Hibernate validation annotation

Sometimes (especially for email addresses) it's important to enforce lowercase. This provides a simple annotation to do that:

```java
@Lowercase
public abstract String getEmail();
```

## @Required Hibernate validation group annotation

Mark fields as required with the `@Required` annotation. Will be used for `validateRequired()` -- see below

## AbstractModel superclass for validations

If your model classes subclass `AbstractModel`, you can call `model.validate()` and `model.validateRequired()`. These methods will use Hibernate Validator to validate all annotated fields on the model, and throw a `ValidationException` if validation fails.

Combined with the `ValidationExceptionMapper` above, you can use a one-liner at the top of your resource methods to ensure that your models are valid. (This is better than using the `@Valid` annotation on the parameter since it is more easily unit-testable and returns a nicer error message.)

Using a superclass is not to everyone's taste, but I like it because sometimes I want to override `validate()` with more detailed rules than can be easily expressed through annotations.
