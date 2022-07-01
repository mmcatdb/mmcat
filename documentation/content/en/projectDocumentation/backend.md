---
title: "Backend Application"
weight: 20
---

The application is located in the `server` package. It's based on the Spring Boot framework.

## Architecture

The application follows a Controller-Service-Repository pattern. Its main purpose is to separate business, presentation and data-storage logic. Although this concept is similar to the widespread MVC pattern, there are some differences.

### Controller

A controller is responsible for providing a REST API for the client application. Whenever a request is received, it's content is parsed and all actual work is delegated to the Service layer. Later, when the request is processed, the response is created, converted to JSON and sent back to the client.

There are several sections (schema category, mappings, jobs, etc.) so each of them has its own controller class. Most of the controllers have their own service classes to delegate the work to and those in turn have their own repository classes.

### Service

A service contains all business logic. It's used by a controller to handle requests. Whenever it needs to fetch or persist data, it delegates this task to a repository.

### Repository

A repository communicates with the database (or multiple database systems, but this application uses only one, PostgreSQL). It basically maps high-level concepts to specific database commands.

## Other structures

The previous pattern is pretty well known in the Java/Node/.Net communities. On the other hand, the following conventions were invented specifically for this application so they definitely can't be considered standard.

### Entity

An entity can be understood as any object that inherits from the `Entity` class, i.e. it has the following property:
```java
public final Integer id;
```
However, in a broader sense, it can also be any object derived from a entity. For example, the id of an entity is known only after it's created by the respective repository. So the initial data from which the entity will be created can't contain the id. Of course we can say that the `null` value means the entity isn't created yet, but this approach enforces us to check the id every time we want to use it.

The solution is to create the same class but without the id property. Although this sounds like a lot of coding, the entities should be only simple objects without any functionalities so it usually isn't a problem.

## View

A concept of view is just further extension of the entity. It encompasses all possible variations of the entity class that are used by specific endpoints. The entity is usually displayed on multiple pages of the client application. For example, one of them displays only the label of the entity, while another needs to show additional data which might be expensive to fetch. The solution is the same as in the previous section - we can create multiple specialized data classes, called views.

However, at this point it's becoming clear that these view aren't any different from entities, because both are just generalized data objects. So the trend is to unite both categories to one. It would be also appropriate to choose a naming convention for these objects.

## Highlights

The whole application is pretty straighforward - it basically just reads the data from the database, provides them to the client, updates them and sometimes calls a transformation algorithm on them. Nevertheless, there are some interesting spots.

### Repository functions

To get data from / to the database is a dangerous business which can lead to many unexpected exceptions. And error handling in java means we have to write many lines of boilerplate `try ... catch` blocks or we end up with `throws Exception` everywhere.

The solution was inspired by the javascript server frameworks, where is quite common to handle requests by the `(request, response) => void` functions. The usage in this application looks like this:
```java
public Entity find(int id) {
    return DatabaseWrapper.get((connection, output) -> {
        // Call the database commands ...
        output.set(new Entity( ... ));
    }
}
```
The `DatabaseWrapper.get` method accepts a function of type `(connection, output) -> void`. The function is expected to use the `connection` to get the data from the database, create the entity objects and then write them to the `output`. The `get` method creates a database connection, executes the function and then returns the result from the `output` object.

There are some other functions like `getMultiple` for exporting arrays and so on but the usage is more or less the same.

### JSON serialization

The framework contains a lot of different data structures which need a to be both persisted in the database and serialized and sent to the client. JSON was selected because it's a data format capable of both. It's also a default format for the client application (which is written in typescript) and probably for most of the REST APIs in general.

Although the data objects are persisted in a relational database, they usually contain only very little relational data. Most of their content is either hierarchical or just plain data. So the solution is to store as much data as it's reasonably possible in the JSON form. This way the same logic can be used for both persisting and transfering data.

However, the serialization isn't that simple. Some of the hierarchical data structures use multiple types of data nodes so it's needed to add the type information to the JSON data. There are also objects which require custom deserialization because they rely on some additional data. To solve this issues, the `JSONConvertible` and `JSONBuilder<T>` interfaces were implemented together with some base classes providing common functionalities. These involve the automatic addition of type data or an error handling.

This approach is quite successful but it requires too much boilerplate code for such a basic functionality. Basically all classes are required to have custom serialization and deserialization classes. Even though the common functionality is inherited, it's still needed to manually serialize all the properties of each class.

However, it recently turnet out that with proper customization the Jackson library is capable of this all on its own. So the trend is to abandon the custom JSON serialization if favor of the more standardized Jackson library.
