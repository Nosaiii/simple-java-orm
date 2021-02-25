# Simple Java ORM

A very compact ORM specifically built for Java

---

## What is it?

SJORM is, like stated before, a very compact Object Relationship Mapper (ORM). It allows developers to integrate this library within their project to easily communicate with their desired database.

## What is an ORM?
Object-relational mapping (ORM, O/RM, and O/R mapping tool) in computer science is a programming technique for converting data between incompatible type systems using object-oriented programming languages. This creates, in effect, a "virtual object database" that can be used from within the programming language. There are both free and commercial packages available that perform object-relational mapping, although some programmers opt to construct their own ORM tools.

<small>Wikipedia contributors. (2021, february 6). Object–relational mapping. Retrieved february 24, 2021, from https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping </small>

---

### Prerequisites

What modules you need to use SJORM

* [Java Development Kit 8](https://www.oracle.com/nl/java/technologies/javase/javase-jdk8-downloads.html) - The JDK development environment for building applications
* An IDE which allows programming in Java (IntelliJ IDEA, Visual Code, Eclipse, etc)
* (Optionally) Maven/Gradle to integrate this library in your project

---

## Installation

*To be continued during future development*

---

## Usage
### Registering the SJORM service
First, it is necessary to create your models, which are basically your tables present in your database that you would like to use in your code. To do this, create a new class for your model and have it extend by `Model` from `com.nosaiii.sjorm`.

```java
@SJORMTable(tableName = "game")
public class Game extends Model {}
```

The `SJORMTable` annotation is used to define what table this model belongs to in your database. It is required for your model to correctly get registered to the SJORM service.

Next, we have to register the SJORM service to set up the database connection and from where you will communicate with your database. Before using SJORM, register a new instance by using the following code:
```java
SJORM.register("127.0.0.1", 3306, "database", "username", "password");
```

This method requires several parameters that are required to connect to your database to communicate with. These are:
1. Host &rarr; The address of the database server
1. Port &rarr; The port of the database server
1. Database &rarr; The name of the database
1. Username &rarr; The username of the login to connect to the database server
1. Password &rarr; The password of the login to connect to the database server

The static `SJORM.register()` method creates an instance of the SJORM service and binds it to a singleton instance that can be accessed through the static `SJORM.getInstance()` at any moment in your code.

Now there is a SJORM instance and it is connected to the specified database.

You had previously created your models, but SJORM is still unaware of them. For that reason, it is necessary to register those models to the SJORM service. To do so, extend your existing code with the following:
```java
SJORM.register("127.0.0.1", 3306, "database", "username", "password");
SJORM.getInstance().registerModel(new ModelMetadata(Game.class));
```

The SJORM service is now aware that the `Game` class is a model that exists in your database.

The static `SJORM.register()` method returns the new instance of the SJORM service. Meaning, it is possible to save it as a variable and access it from there as well, instead of using the static `SJORM.getInstance()` method. Either way, both options return the same instance of the SJORM service.

Setting up the SJORM service is now all done!

### Retrieving models
To query data from your database and get them as a collection of models, you can use the available methods in the SJORM service. One of the straight-forward methods is the `getAll()` method.
```java
Query<Game> games = sjorm.getAll(Game.class);
```

A `Query<T>` is a class in which you can query on a collection, and it contains instances of your pre-defined models. It contains methods that you can use to manipulate the entries in the collection. It is an equivalent and simplified version of LINQ from C# (<https://docs.microsoft.com/en-us/dotnet/csharp/programming-guide/concepts/linq/>) and somewhat comparable to Stream that was released in Java 8. It makes use of method-chaining, meaning you can chain the methods together and call them one after another, while still returning a `Query<T>`.

See some examples below on what you can do with a `Query<T>` instance:
```java
Query<Game> games = sjorm.getAll(Game.class);

// Get the amount of games
int gameAmount = games.count();

// Get the first Game instance from the collection
Game game = games.first();

// Get a list of all names of the games
List<String> gameNames = games.select("name");

// Get a list of games ordered by id
Query<Game> gamesOrderedById = games.orderBy("id");

// Get all games where the name starts with an 'A'
Query<Game> gamesStartingWithA = games.where(g -> g.getProperty("name", String.class).matches("[aA].*"));

// Loop over all Game instances
for(Game game : games.toList()) {
    // Do something with 'game'
}
```

A custom `Query<T>` can be instantiated from an instance of Java's `List<T>` class, by using the constructor that requires a `List<T>`, as shown below:
```java
List<String> names = new ArrayList<>(Arrays.asList("John", "Kim", "Danny"))
        
Query<String> queryNames = new Query(names);
```

To access the fields of a model, you will have to use its properties. The properties are stored in a `LinkedHashMap` that can be accessed by using either `Model#getProperty()` or `Model#setProperty()`. Where the latter acts as a setter and the former as a getter. There are two variations of the `Model#getProperty()` method. One where you can get the value of the property as an `Object` type, and the other where you can specify an explicit type cast to get the property as its correct data type. See the example below:
#### Manual casting
```java
// Get the first game from the database
Game game = SJORM.getInstance().getAll(Game.class).first();

// Get properties from the model
Long id = (Long) game.getProperty("id");
String name = (String) game.getProperty("name");
Long minPlayers = (Long) game.getProperty("min_players");
Long maxPlayers = (Long) game.getProperty("max_players");
```

#### Explicit casting
```java
// Get the first game from the database
Game game = SJORM.getInstance().getAll(Game.class).first();

// Get properties from the model
Long id = game.getProperty("id", Long.class);
String name = game.getProperty("name", String.class);
Long minPlayers = game.getProperty("min_players", Long.class);
Long maxPlayers = game.getProperty("max_players", Long.class);
```

**Note**: When using explicit casting to get properties, you can still get a `java.lang.ClassCastException` thrown when Java was unable to cast the value of your property to the given type.

### Creating models
It is possible to create new models and save them directly to your database using your pre-defined model classes. All you have to do is create a new instance of the class with a parameterless constructor and you are good to go.
```java
// Create a new model instance
Game game = new Game();

// Set its required properties
game.setProperty("name", "New super awesome game");
game.setProperty("min_players", 8);
game.setProperty("max_players", 24);
```

### Saving models
SJORM can be used to change the properties of models and then save them directly to the database. Calling the method `Model#save()` will save the changes of the model to your database. SJORM automatically detects if a model already exists or not in the database. By doing this, it will determine whether to use an `INSERT INTO` or an `UPDATE`.
```java
// Get the first game from the database
Game game = SJORM.getInstance().getAll(Game.class).first();
game.setProperty("name", "First game");
game.save();
```

### Virtual properties
*To be continued during future development*

### Set up relationships
*To be continued during future development*

---

## Examples

*To be continued during future development*

---

## Authors

* **Jason van der Lee** - *Head developer* - [Nosaiii](https://github.com/Nosaiii)

See also the list of [contributors](https://github.com/Nosaiii/simple-java-orm/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details