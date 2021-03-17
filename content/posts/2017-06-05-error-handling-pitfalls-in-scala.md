---
title: Error handling pitfalls in Scala
publishdate: 2017-06-05
tags:  [scala]
---

There are multiple strategies for error handling in Scala.

Errors can be represented as [exceptions](http://alvinalexander.com/scala/scala-try-catch-finally-syntax-examples-exceptions-wildcard), which is a common way of dealing with errors in languages such as Java. However, exceptions are invisible to the type system, which can make them challenging to deal with. It’s easy to leave out the necessary error handling, which can result in unfortunate runtime errors.

In Scala, it is usually recommended to represent errors as part of the return type. Scala’s standard library types such as [Either](http://danielwestheide.com/blog/2013/01/02/the-neophytes-guide-to-scala-part-7-the-either-type.html) and [Try](http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html) can be used for capturing errors in synchronous operations, while [Future](http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html) can be used for representing asynchronous operations. Furthermore, Scala programmers can also represent different outcomes as a custom type using [sealed trait](http://alvinalexander.com/scala/benefits-of-sealed-traits-in-scala-java-enums) capabilities.

Return type based error handling has its own problems. In this article, I’ll demonstrate how you can fall into a pit with types like `Either` and `Try` by accidentally hiding the error scenarios. To avoid the pit, I’ll then describe how alternative constructs and code linting can mitigate the presented problems.

<!--more-->

## Ways to accidentally hide errors

Unless you’re using custom types for representing different outcomes, the return type you use for capturing errors most likely comes with methods for composition: `map`, `flatMap`, and `withFilter`. With these methods in place, we can leverage Scala’s for-comprehensions when building programs which can produce errors.

``` scala
// Example #1
def foo(i: Int): Try[String] = ???

def bar(): Try[String] =
  for { // equivalent to foo(1).flatMap(x => foo(2).map(y => x + y))
    x <- foo(1)
    y <- foo(2)
  } yield x + y
```

In the example above (example \#1), `foo` can produce an error, which is represented in the `Try` return type. When we compose the operations together using `for` / `flatMap`, we’re describing how the program should work in the happy path scenario. If at any point an error value is met, the functions following it are not executed, and the error value is escalated as the return value. Using these composition methods, we can easily create programs that have flows similar to more traditional exception based error handling, and at the same time we can have the errors represented in the return type. This sequential composition style is called [the Monadic style](https://darrenjw.wordpress.com/2016/04/15/first-steps-with-monads-in-scala/).

As we can see from the example program, the composition is a manual step that the developer has to fulfil. What would happen if parts of our program don’t follow the composition flow?

``` scala
// Example #2
def foo(i: Int): Try[String] = ???

def bar(): Try[String] = {
  val xf = foo(1)
  val yf = foo(2)
  val zf = foo(3) // unused

  for {
    x <- xf
    y <- yf
  } yield x + y
}
```

The example above (example \#2) is very similar to example \#1. Both examples compose the same results, and the happy path scenario works the same.

There are a couple of differences how these examples work at runtime. In example \#2, the result of `foo(3)` is assigned the name `zf`, but its result is never used. Because it’s never used, it will also not affect the result of `bar` at all. Therefore, any error that `foo(3)` might produce will be completely silent.

Another property of example \#2 is that we’ve made multiple calls of `foo`, but we’ll only capture at most one of the errors that might occur when we run the program. This is because the composition will always end when it meets the first error. For example, if both `xf` and `yf` contain an error, then the result of `bar` will be the same as `xf`.

In many cases, capturing these error cases is vital. If `foo` has no side effects (e.g. `foo` is for computing the square root of a number), the function call simply wastes some computing resources for the duration of the function call. However, if `foo` does have side effects (e.g. `foo` writes the input to the database), we most likely want to at least capture the error it might have produced rather than let it silently fail in the background.

What makes these properties problematic is how easy it is to get yourself in trouble. Slightly altering the placement in which these function calls are made creates subtle differences in how (or whether) the error cases are handled. Often we’d like both of the styles presented in these examples to work the same at runtime.

These properties are not limited to just `Try`. Both `Either` and `Future` share the same properties.

It’s also important to highlight that you can also get yourself into trouble by using some of the methods in these types. For example, `Try`, `Either`, and `Future` all have the method [foreach](http://www.scala-lang.org/api/current/scala/util/Try.html#foreach%5BU%5D\(f:T=%3EU\):Unit), which will execute the given function only for successful results, but it will completely ignore the failure scenario:

``` scala
def foo(i: Int): Try[String] = ???

def bar() = { // the type is Unit
  val x = foo(2)
  x.foreach(println(_))
}
```

## In search of an alternative solution

The problem we’re seeing with `Either`, `Try`, and `Future` is that all of them are evaluated eagerly, and at the same time they can cause side effects.

These properties seem problematic together. What if our computations had only one of these properties? If the computation can have side effects, but it’s not evaluated until examined, we could build our computations so that the side effects will not get executed until there’s an error handling in place. On the other hand, if the computation can’t have side effects, the worst you do cause is waste some CPU cycles and memory.

Let’s explore these combinations, and see what solutions we can find.

### Side effects with lazy evaluation

Scala’s `Try` values are evaluated eagerly. Let’s create our own version of `Try` that is lazy evaluated. When the value is examined, it produces a Scala `Try` as a result.

``` scala
import scala.util.{Try, Success, Failure}

// WARNING: This is only a demo!
final class Attempt[A](proc: => A) {

  def evaluate(): Try[A] = Try(proc)

  def evaluateUnsafe(): A = proc

  def map[B](f: A => B): Attempt[B] =
    new Attempt[B](f(proc))

  def flatMap[B](f: A => Attempt[B]): Attempt[B] =
    new Attempt[B](f(proc).evaluateUnsafe())

  def withFilter(f: A => Boolean): Attempt[A] =
    new Attempt[A]({
      val r = proc
      if (f(r)) r
      else throw new NoSuchElementException("filter == false")
    })
}

object Attempt {
  def apply[A](proc: => A): Attempt[A] = new Attempt(proc)
}
```

In the code listing above, we’ve defined our own version of `Try` called `Attempt`. `Attempt` is given a procedure as a parameter, which can either produce a value or throw an exception when evaluated. The procedure can be evaluated in two ways: Evaluating it safely will produce the result of the procedure wrapped in a standard Scala `Try`, while evaluating it unsafely will escalate any errors as exceptions. In order to make `Attempt` composable, it also has definitions for the `map`, `flatMap`, and `withFilter` methods.

Let’s see `Attempt` in action. We’ll define a program with `Attempt` which performs side effects, and verify that only the side effects part of the flow will get executed.

``` scala
def attemptPrint(s: String): Attempt[Unit] = Attempt(println(s))

def attemptOkExample: Attempt[Int] = {
  val _ = attemptPrint("I won't be printed")
  for {
    x <- Attempt(1)
    _ <- attemptPrint("x = " + x)
    y <- Attempt(2)
    _ <- attemptPrint("y = " + y)
  } yield x + y
}

attemptOkExample.evaluate() match {
  case Success(i) => println("Got: " + i)
  case Failure(ex) => println("Failed: " + ex.getMessage)
}
```

In the example above, the flow of our program prints two numbers and sums them. As part of the same function call, and outside of the flow, there’s another print command wrapped in an `Attempt`. Since `Attempt` is lazy evaluated, the rogue print command will not be executed at all. We can verify this from the program output:

```
x = 1
y = 2
Got: 3
```

We could create a similar solution for Scala `Future`, which would provide us lazy evaluated computations in an asynchronous context. Instead of implementing everything ourselves, we can leverage existing solutions from third party libraries:

  - [ScalaZ](https://github.com/scalaz/scalaz) provides [Task](http://timperrett.com/2014/07/20/scalaz-task-the-missing-documentation/)
  - [Cats](https://github.com/typelevel/cats) provides [IO](https://github.com/typelevel/cats-effect)
  - [Monix](https://monix.io/) provides [Task](https://monix.io/docs/2x/eval/task.html)

All three of them are lazy rather than eager. ScalaZ Task and Cats IO are designed for both synchronous and asynchronous computations, while Monix Task is designed for asynchronous computations only.

### No side effects with eager evaluation

Scala programs can have side effects at any point of the code. The programmer can rigorously follow the [idiom](http://alvinalexander.com/scala/scala-idiom-methods-functions-no-side-effects) of limiting the use of side effects, but there’s not much the compiler can do to help. However, if one decides to follow this idiom, Scala’s standard library types will be enough to avoid the problems presented earlier. The limitation here is of course that we now have no way to express effects within our computations.

### No side effects with lazy evaluation

Effects are incredibly useful, and usually we’d like to have at least some way to express them in our programs. However, combining eager evaluation with effects leads us back to the problem we presented earlier. Can we build a system where effects are expressed as pure computations instead?

Earlier, we explored a few solutions for computations which are lazy and have side effects. What if we were to eliminate both side effects and eager evaluation?

In this system, all the effects would be represented as values as opposed to side effects. These values can then be composed together to create programs. When we are ready to execute our program, we run it through an interpreter that translates our values into actual side effects.

One way to implement this kind of a system is to use [Free monads](http://typelevel.org/cats/datatypes/freemonad.html). The topic of Free monads is large enough to require an article of its own. For more in-depth information, I recommend reading [Pere Villega’s overview on Free monads](http://perevillega.com/understanding-free-monads).

## “I’m stuck with Either/Try/Future\! What do I do?”

Not every project can switch to alternative computation types such as `Task`. Even fewer projects can start using Free monads. The project might either be heavily tied to existing Scala types or it simply cannot afford to include another dependency.

Whatever the reason may be, it’d be still nice if we could detect some of the problems presented in this article. Having knowledge of the problem combined with a proper code review process helps, but it’d be even better to have something we could automate.

The Scala compiler provides a few helpful compiler flags which can help detect potential problems, by producing warnings during compilation:

  - `-Ywarn-dead-code` helps you detect any unreachable code.
  - `-Ywarn-unused:locals` (2.12.x) and `-Ywarn-unused` (2.11.x) help detect unused values in functions.
  - `-Ywarn-value-discard` helps detect when non-Unit results are unused. If you really don’t need the value, you can explicitly assign it to `_` to make it clear you want to discard the value. Keep in mind though that you cannot assign multiple values to `_` due to [SI-7691](https://github.com/scala/bug/issues/7691). To get around this issue, you can create a function or a method which discards the value for you.
  - `-Xfatal-warnings` turns all the warnings into compile errors. This way you can enforce conformance to these rules at build time.

The flags will not detect all the problems on their own. For example, there’s no flag for detecting when you compose two already executed results. However, they can still be useful for highlighting some of the common problems.

If you want to find more information on how compiler flags can help you detect problems, I recommend checking out Rob Norris’ Scala flags recommendations for Scala versions [2.12](https://tpolecat.github.io/2017/04/25/scalac-flags.html) and [2.11](https://tpolecat.github.io/2014/04/11/scalac-flags.html). In addition to the compiler flags, you can use tools such as [WartRemover](http://www.wartremover.org/) for finding additional code lint.

## Conclusions

In this article, I’ve explored some of the pitfalls of Scala’s standard library types for error handling. I’ve presented alternative constructs that avoid the presented problems as well as utilities for detecting them.

While there are definitely problem areas in Scala’s standard library types, they’re still incredibly useful for many situations. Compiler flags and linting can help detect problems in many cases, but larger codebases will benefit from alternative constructs that avoid the problem completely.

One area I didn’t explore in this article was error handling in types that don’t follow the Monadic style of composition. Types such as [ScalaZ Validation](https://www.47deg.com/blog/fp-for-the-average-joe-part-1-scalaz-validation/) and [Cats Validated](https://sihil.net/cats-validated.html) can be composed in [the Applicative style](https://softwaremill.com/applicative-functor/), which provides you capabilities for aggregating errors.

As always, I’ve published the code examples in a [GitHub Gist](https://gist.github.com/jkpl/a91d4b93c452efc20f2c866ac3a2c577). Thanks for reading\!
