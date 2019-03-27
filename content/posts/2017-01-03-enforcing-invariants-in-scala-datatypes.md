---
title: Enforcing invariants in Scala datatypes
publishdate: 2017-01-03
tags: [scala]
---

Scala provides many tools to help us build programs with less runtime
errors. Instead of relying on nulls, the recommended practice is to use
the `Option` type. Instead of throwing exceptions, `Try` and `Either`
types are used for representing potential error scenarios. What's
common with these features is that they're used for capturing runtime
features in the type system, thus lifting the runtime scenario handling
to the compilation phase: your program doesn't compile until you've
explicitly handled nulls, exceptions, and other runtime features in your
code.

In his "Strategic Scala Style" blog post series, Li Haoyi explores
[patterns for enforcing invariants in datatypes](http://www.lihaoyi.com/post/StrategicScalaStyleDesigningDatatypes.html)
using techniques such as self-checks and structural enforcement. Some of
the techniques described in the blog post rely on runtime assertions.
Thus, while they prevent invalid data from appearing in your datatypes,
the datatype construction is not fully represented in the type system.

In this blog post, I'll expand the techniques by demonstrating ways to
enforce invariants using type-safe, compile-time techniques. First,
I'll demonstrate how the runtime assertion style works using an example
datatype. I'll then show how we can enforce invariants in the example
datatype in a type-safe style. Finally, I'll briefly cover the pitfalls
of using case classes for datatypes with type-safe invariant validation,
and how we can regain the case class features using tricks in the class
inheritance system and ScalaMeta macros.

<!--more -->

Invariant validation using runtime assertions
---------------------------------------------

As an example, let's create a datatype for conferences. A conference
has a name, a start date, and an end date. The name of the conference
cannot be empty, and the end date cannot be before the start date. Using
the runtime assertion technique, the datatype could be represented as
follows:

``` {.scala}
import java.time.LocalDate

case class Conference(name: String, startDate: LocalDate, endDate: LocalDate) {
  require(name.nonEmpty)
  require(startDate.isEqual(endDate) || startDate.isBefore(endDate))
}
```

In the example above, we use Scala's builtin function `require` to
ensure our datatype complies to our requirements. In order to ensure
we've captured the potential runtime errors, we can either wrap the
datatype construction with a try-catch block or the `Try` constructor:

``` {.scala}
try {
  val invalidConference = new Conference("", LocalDate.now(), LocalDate.now())
} catch {
  case ex: IllegalArgumentException =>
    // Handle the error in any way you like here
    println("Invalid parameters when creating a conference: " + ex.getMessage)
}

val invalidConference: Try[Conference] = Try(new Conference("", LocalDate.now(), LocalDate.now()))
```

The issue here is the same as with any other function that throws
errors: the function is partial, not total. This means that only a
subset of inputs will produce a value corresponding to the expected type
while the rest of the inputs will cause the program execution to escape
the normal program flow. The outcome of the invalid inputs is not
represented in the type system, thus it is not type-safe.

In practice, the issue with invariant validation that is not type-safe
is that it puts the burden of capturing errors on the user's side.
Failing to capture errors produces non-working software in places where
you'd normally expect them work just fine. As the places where the
datatype is constructed increases, so does the chance of failing to
capture the errors.

Enforcing invariants in a type-safe style
-----------------------------------------

In order ease the burden of manually catching errors, we can make the
invariant validation type-safe by representing the potential runtime
errors in the type system. This can be done by providing the users a
constructor that returns either the constructed value (an instance of a
conference) or an error value.

``` {.scala}
class Conference private (
  name: String,
  startDate: LocalDate,
  endDate: LocalDate
)

object Conference {
  def apply(name: String, startDate: LocalDate, endDate: LocalDate): Option[Conference] =
    if (name.nonEmpty && (startDate.isEqual(endDate) || startDate.isBefore(endDate))) {
      Some(new Conference(name, startDate, endDate))
    } else {
      None
    }
}

val validConference: Option[Conference] = Conference("ScalaDays 2017", LocalDate.of(2017, 5, 30), LocalDate.of(2017, 6, 2))
val invalidConference: Option[Conference] = Conference("", LocalDate.now(), LocaDate.now())
```

In the example above, we've defined a class for the conference
datatype, and an alternative constructor function in the datatype's
companion object that validates the input. For the sake of simplicity,
we've represented the return type of the alternative constructor as an
`Option[Conference]`, where the absence of the value represents a
validation error.

We've also hidden the primary constructor of the datatype. This way the
constructor can only be used in the scope of the alternative constructor
to create validated instances. If the constructor is not hidden, invalid
instances may be created by the user accidentally using the wrong
constructor. Hiding the primary constructor also effectively prevents
the extension of the class.

A more detailed representation of the construction could include more
details about the invariant validation errors. When using the `Either`
type, one of the sides in the type could be used for representing one or
more validation errors, while the other side would represent the
successful value. For example in `Either[List[Error], Conference]`, the
left-side represents all of the errors found during construction, and
the right-side represents the successfully built instance of a
conference. Regardless of which return type is used, the pattern for
building type-safe values is similar: the return value must be
"unwrapped" to reach the actual value.

We can further enhance the validation process by creating meaningful
datatypes for the datatype's fields. For example, the name of the
conference can made to its own datatype with its own validation rules:

``` {.scala}
class Name private (val name: String) extends AnyVal

object Name {
  def apply(name: String): Option[Name] =
    if (name.nonEmpty) Some(new Name(name))
    else None
}
```

Here the name is encapsulated in a single value class. By making the
datatype a value class (i.e. it extends `AnyVal`), we can in some cases
avoid the overhead of allocating the object around the string, but at
the same time enjoy the type-safety it provides compared to plain
strings. As a value class, the class will also automatically have
`equals` and `hashCode` methods based on the type it wraps.

With the `Name` type in place, we can make the `Conference` type use it
without having to repeat the validation process.

``` {.scala}
class Conference private (
  name: Name,
  startDate: LocalDate,
  endDate: LocalDate
)

object Conference {
  def apply(name: Name, startDate: LocalDate, endDate: LocalDate): Option[Conference] =
    if (startDate.isEqual(endDate) || startDate.isBefore(endDate)) {
      Some(new Conference(name, startDate, endDate))
    } else {
      None
    }
}

val validConference: Option[Conference] = for {
  name <- Name("ScalaDays 2017")
  conference <- Conference(name, LocalDate.of(2017, 5, 30), LocalDate.of(2017, 6, 2))
} yield conference
```

Escaping invariant validation in case class datatypes
-----------------------------------------------------

In Scala, case classes bring a bag full of goodies to regular old
classes. For example, case classes have an automatically implemented
`equals`, `hashCode`, `toString` methods, and an extractor for pattern
matching based on the class fields.

Case classes are convenient for implementing datatypes, so it makes
sense to use them as the base type for datatypes where invariants are
validated in a type-safe way. However, they also make it easy to bypass
the validation checks. Unlike regular classes, case classes have more
than one constructor that need hiding. Besides the regular constructor,
case classes can also be constructed using their auto-generated `apply`
function and `copy` methods.

``` {.scala}
case class Conference private (
  name: String,
  startDate: LocalDate,
  endDate: LocalDate
)

object Conference {
  def create(name: String, startDate: LocalDate, endDate: LocalDate): Option[Conference] =
    if (name.nonEmpty && (startDate.isEqual(endDate) || startDate.isBefore(endDate))) {
      Some(new Conference(name, startDate, endDate))
    } else {
      None
    }
}

// Invalid instance through apply function
val invalidConference1: Conference = Conference("", LocalDate.now(), LocalDate.now())

// Valid instance...
val Some(validConference) = Conference.create("ScalaDays 2017", LocalDate.of(2017, 5, 30), LocalDate.of(2017, 6, 2))

// ...can be a gateway to an invalid instance.
val invalidConference2: Conference = validConference.copy(name = "")
```

In the example above, we've replaced the normal `Conference` class with
a case class. We can still create validated instances of the datatype
using the `create` function in the companion object. However, we can
still access the auto-generated `apply` method, and create invalid
instances that way. Moreover, we can use the `copy` method to create an
invalid copy of the datatype from a valid datatype.

The `copy` method can be erased by manually overriding it with a method
that doesn't produce invalid copies, but the `apply` function can
neither be overridden or hidden. The inability to replace the function
is why the validating constructor function has to occupy a different
name. These features make case classes unfeasible for datatypes that
enforce invariants in a type-safe way.

Regaining the case class features
---------------------------------

As mentioned earlier, case classes contain a lot of useful features.
Thus it would be a shame to lose them just because their `apply` and
`copy` constructors clash with the invariant validation process. How can
we keep the case class features that don't conflict with the
validation?

One way to have case classes without `apply` and `copy` constructors is
to make the case classes [abstract and
sealed](https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2).
Like any other class, case classes can be made abstract. This means that
the class may define function interfaces that are left open for
implementation in its subclasses. It also means that the class must be
extended as no instance of it can be created directly even when there's
nothing left for the subclass to implement. Therefore, `apply` or `copy`
constructors are not generated for the case class because there's no
known constructor to base their implementation on.

Because there is no `final` modifier for limiting subclassing with
traits and `abstract` classes, we'll have to use the `sealed` modifier
instead. The `sealed` modifier allows us to limit the scope in which
subclasses are made to the source file. Thus, no instances can be made
without accessing the constructors we provide.

``` {.scala}
sealed abstract case class Conference(name: String, startDate: LocalDate, endDate: LocalDate)

object Conference {
  def apply(name: Name, startDate: LocalDate, endDate: LocalDate): Option[Conference] =
    if (name.nonEmpty && (startDate.isEqual(endDate) || startDate.isBefore(endDate)))
      Some(new Conference(name, startDate, endDate) {})
    else
      None
}
```

In the example above, we've recreated the earlier `Conference`
datatype, but this time as a sealed abstract case class. Our only means
of constructing an instance is through the custom constructor we
provide. However, as the datatype is now a case class, we can use its
case class features such as auto-generated `equals` and `hashCode`
functions.

An alternative way to get case class features in a regular class is to
use [ScalaMeta](http://scalameta.org/)'s `@data` annotation macro. The
`@data` annotation macro automatically generates case class features for
any regular class that uses the annotation. The annotation allows
selecting which case class features are included for each class.

In order to use the annotation, we first need to enable ScalaMeta and
the [paradise compiler plugin](https://github.com/scalameta/paradise)
for the project. Here's an example of what needs to be added to our SBT
configuration:

``` {.scala}
// Resolvers for the ScalaMeta library and paradise plugin
resolvers in ThisBuild += Resolver.url("scalameta", url("http://dl.bintray.com/scalameta/maven"))(Resolver.ivyStylePatterns)

// Enable paradise compiler plugin
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0.138" cross CrossVersion.full)
scalacOptions += "-Xplugin-require:macroparadise"

// Add library dependency for ScalaMeta
libraryDependencies += "org.scalameta" %% "scalameta" % "1.3.0"
```

After enabling the library and the compiler plugin, we can use the
`@data` annotation.

``` {.scala}
@data(copy = false, apply = false)
class Conference private (name: String, startDate: LocalDate, endDate: LocalDate)

object Conference {
  def apply(name: String, startDate: LocalDate, endDate: LocalDate): Option[Conference] =
    if (name.nonEmpty && (startDate.isEqual(endDate) || startDate.isBefore(endDate)))
      Some(new Conference(name, startDate, endDate))
    else
      None
}
```

In the example above, we've annotated the `Conference` class with the
`@data` annotation. By default, all of the case class features are
enabled for the class. Here we've disabling the `copy` and `apply`
constructors, thus we can have full control of the constructors that are
visible to the datatype user.

Conclusions
-----------

In this article, I've demonstrated a type-safe way for enforcing
invariants in Scala datatypes. I've shown common techniques for
invariant validation that are not type-safe and the issues commonly
encountered with them. I've also shown the pitfalls of using case
classes in combination with the type-safe invariant validation, and how
those problems can be avoided using class inheritance and ScalaMeta
macros.

As usual, I've made examples for the different invariant validation
techniques available in a [Github Gist](https://gist.github.com/jkpl/4932e8730c1810261381851b13dfd29d) to
play around with. Thanks for reading!
