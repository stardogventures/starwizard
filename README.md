# starwizard

Supporting classes for Dropwizard REST APIs

Dropwizard is awesome, and I've found it incredibly useful for building simple, scalable REST APIs. But while Dropwizard is opinionated, there are some things that it (appropriately) doesn't include in the box, which I've found I tend to re-use over and over when building Dropwizard APIs.

* I usually run Dropwizard APIs on AWS behind an Elastic Load Balancer. ELB doesn't have an automatic way to force HTTPS, so I always need an easy way to redirect HTTP requests to HTTPS.
* I use [Swagger](http://swagger.io/) to document my APIs, using the excellent [dropwizard-swagger](https://github.com/smoketurner/dropwizard-swagger) bundle for swagger-core. However, swagger-core doesn't provide an easy way to exclude Dropwizard @Auth parameters from the Swagger definitions.
* I always want an easy way to validate my models, with the option to validate for required fields. A basic AbstractModel superclass, combined with a `Required` validation group, allows me to validate models in one line of code. To top it off, a `ValidationExceptionMapper` renders helpful error messages.

Here are all these reusables in one place. Hope someone else finds it useful!

Some of these classes are not of my invention:
  * `AuthParamFilter` is from this  excellent blog post by [Pablo Meier](https://github.com/pablo-meier): https://www.reonomy.com/augmenting-dropwizard-with-swagger/, based on this StackOverflow answer by [Özkan Can](http://stackoverflow.com/users/2494590/%C3%96zkan-can) http://stackoverflow.com/questions/21911166/how-can-i-set-swagger-to-ignore-suspended-asyncresponse-in-asynchronous-jax-rs

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

On models, mark fields as required with a `@NotNull(groups = Required.class)` annotation. Will be used for `validateRequired()` -- see below

## AbstractModel superclass for validations

If your model classes subclass `AbstractModel`, you can call `model.validate()` and `model.validateRequired()`. These methods will use Hibernate Validator to validate all annotated fields on the model, and throw a `ValidationException` if validation fails.

Be sure to add the exception mapper to your main class:

```java
env.jersey().register(new ValidationExceptionMapper());
```

Now you can use a one-liner at the top of your resource methods to ensure that your models are valid. (This is better than using the `@Valid` annotation on the parameter since it is more easily unit-testable and returns a nicer error message.)

Using a superclass is not to everyone's taste, but I like it because sometimes I want to override `validate()` in the subclass with more detailed rules than can be easily expressed through annotations.
