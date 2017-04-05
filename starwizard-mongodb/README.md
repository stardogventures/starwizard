# starwizard-mongodb module

### Reusable classes for Dropwizard and MongoDB

Included are some supporting classes that I've found helpful when building a Dropwizard service that communicates with MongoDB.

For other useful Dropwizard reusables (not related to MongoDB), take a look at [starwizard-core](https://github.com/stardogventures/starwizard).

If you are interested in using a lightweight MongoDB DAO abstraction with support for POJO mapping using Jackson, take a look at [stardao](https://github.com/stardogventures/stardao).

## Installation

Add the following to your POM:

```
<properties>
    <starwizard.version>0.1.3</starwizard.version>
    ...
</properties>

<dependencies>
  <dependency>
    <groupId>io.stardog.starwizard</groupId>
    <artifactId>starwizard-mongodb</artifactId>
    <version>${starwizard.version}</version>
  </dependency>
  ...
</dependencies>
```

## Features and usage

### MongoHealthCheck

This Dropwizard [health check](http://www.dropwizard.io/1.1.0/docs/manual/core.html#health-checks) checks that your service has basic connectivity to a MongoDB database, by issuing the MongoDB [ping](https://docs.mongodb.com/manual/reference/command/ping/) command.

To use it, just register it in your app's health checks in your `run()` method.

```
env.healthChecks().register("mongo", injector.getInstance(MongoHealthCheck.class));
```

### Swagger ObjectIdConverter

[swagger-core](https://github.com/swagger-api/swagger-core), by default, gets confused by the `ObjectId` MongoDB type, and will expose it as a total mess of an object containing the timestamp, machine identifier, etc. Normally in your APIs, you want `ObjectId` to be considered a string for external consumption.

This `ModelConverter` will force ObjectIds to be treated as Strings for Swagger's purposes.

To use it, add it to the `ModelConverters` instance:

```
ModelConverters.getInstance().addConverter(new ObjectIdConverter());
```
