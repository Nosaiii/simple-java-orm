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
public class Game extends Model {
    public Game(ResultSet rs) {
        super(rs);
    }

    public Game() {
        super();
    }
}
```

Note that the compiler will not give you an error about the missing constructor if you had not defined it. The reason for this is that `Model` contains two constructors; one for instantiating a model and one on retrieving data from the database when generating model objects. It is necessary to include both constructors in the inherited model class and to call both of their super constructors, as shown in the example above.

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
for(Game game : games) {
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

### Set up relationships
In order to map your relationship between models, you will have to define them in your model class. It is a matter of returning the result of the relationship of what it refers to (single model or a collection) depending on the type of relationship.

SJORM's relationship system is built for two very specific relationship types:
- One-to-many &rarr; The model refers to a collection of related models where the foreign key is stored in the target table
- Many-to-many &rarr; The model refers to a collection of related models. The same counts for the other way around. There is a pivot table between holding the foreign keys.

If we take our Game model, and we also create a Team model, which holds just a name for the team, and a foreign key referencing to what game it belongs to, we can define their relationship using SJORM as follows:

**Game.java**:
```java
@SJORMTable(tableName = "game")
public class Game extends Model {
    public Game(ResultSet resultSet) {
        super(resultSet);
    }

    public Query<Team> getTeams() {
        return hasMany(Team.class);
    }
}
```

**Team.java**:
```java
@SJORMTable(tableName = "team")
public class Team extends Model {
    public Team(ResultSet rs) {
        super(rs);
    }

    public Team() {
        super();
    }

    public Game getGame() {
        return belongsTo(Game.class);
    }
}
```

The method `Model#hasMany` assumes that the foreign key is stored in the target table. In the case above, the foreign key is stored in the Team model. The method `Model#belongsTo` assumes that the foreign key is stored in its own table, thus a Team model only has one single relationship to a Game.

If your database makes use of a Many-to-many relationship with a pivot table between two tables, you will have to also define the pivot table as its own model and have it extend from `PivotModel`. This type of model has extended functionality, compared to a regular `Model` super class to support Many-to-many relationships.

Assume a Game model can have multiple tags associated to it and the same counts for the other way around. In this case, a pivot table will be used containing foreign keys using ids to identify what games belong to what tag and what tags belong to what game. For this case, use the `Model#hasManyPivot` and pass the class type of a pivot model as reference. See this use case in the example below:
**Game.java**:
```java
@SJORMTable(tableName = "game")
public class Game extends Model {
    public Game(ResultSet resultSet) {
        super(resultSet);
    }

    public Query<Team> getTags() {
        return hasManyPivot(GameTag.class);
    }
}
```

**Tag.java**:
```java
@SJORMTable(tableName = "tag")
public class Tag extends Model {
    public Tag(ResultSet rs) {
        super(rs);
    }

    public Tag() {
        super();
    }

    public Query<Game> getGames() {
        return hasManyPivot(GameTag.class);
    }
}
```

**GameTag.java**: (Pivot table)
```java
@SJORMTable(tableName = "game_tag")
public class GameTag extends PivotModel {
    public GameTag(ResultSet rs) {
        super(rs, Game.class, Tag.class);
    }

    public GameTag() {
        super(Game.class, Tag.class);
    }
}
```

As you can see, you can directly get a collection of its related models from both sides without first having to refer to the underlying pivot model.

### Virtual properties
*To be continued during future development*

---

## Examples

*To be continued during future development*

---

## Tests
Tests are being executed with usage of [JUnit 4](https://junit.org/junit4/). Run the tests of SJORM by running the gradle testing script. The tests of SJORM are located under the _com.nosaiii.sjorm_ package within the _test_ folder in the project's source.

For contributing to the project, all tests must pass and tests have to be added that correspond to the modified code of a branch being pulled to the _develop_ (and _main_) branch. If not, the pull request of the branch will not be approved for merging.

---

## Authors

* **Jason van der Lee** - *Head developer* - [Nosaiii](https://github.com/Nosaiii)

See also the list of [contributors](https://github.com/Nosaiii/simple-java-orm/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details