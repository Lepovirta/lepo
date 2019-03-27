---
title: Reusable response collectors in Akka
publishdate: 2016-04-07
tags: [scala, akka]
---

One of the recommended practices in Akka is to use the
[response collector pattern](http://techblog.net-a-porter.com/2013/12/ask-tell-and-per-request-actors/)
instead of the [ask pattern](http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Ask__Send-And-Receive-Future)
for request-response style actor communication. It simplifies actor
communication by placing most of the response management under one
specialised actor. Instead of adding timeout and error handling logic to
several places in the actor system, the actors simply respond to any
queries using the standard tell mechanism while the response collector
actor decides when it has received enough responses.

It's easy to create an ad hoc response collector, but in many cases it
would be nice to be able to reuse existing actor code.

In this article, I'll present a reusable response collector actor.
First, I'll go over some of the common patterns that can be seen in
many response collectors, and form an API for those patterns. After
that, I'll present an implementation for the response collector actor
that's using the API. Finally, I'll demonstrate a few examples of
using the actor.

<!--more-->

Common patterns in response collectors
--------------------------------------

Response collector behaviour can be summarised as follows: collect
relevant responses until all the results have been collected or a
timeout has been exceeded, and make the results available to a consumer.
There's quite a few requirements in that sentence. Let's see if we can
extract individual patterns from it.

### Collecting relevant responses

One caveat of Akka actors is that Akka doesn't enforce type safety in
the processing of incoming messages. Specifically, each message handler
has to be able to handle any type of incoming message. There are a few
attempts at bringing type safety to Akka actors (see [Typed
Actors](http://doc.akka.io/docs/akka/2.4.1/scala/typed-actors.html) and
[Akka
Typed](http://doc.akka.io/docs/akka/2.4.1/scala/typed.html#typed-scala)),
but for the purpose of this exercise we'll just collect the interesting
messages, and log an error for all the unexpected messages.

Scala's `PartialFunction` is useful for matching against a partial set
of inputs. User can create a matcher of type `PartialFunction[Any, T]`
where the type parameter `T` is the type of the expected output. For
example, the following matcher can be used for extracting only incoming
strings:

    val matcher: PartialFunction[Any, String] = {
      case s: String => s
    }

### Determining completion

How can a response collector decide when it has collected enough
responses? It depends on the use-case. Sometimes we're interested in
collecting a certain set of responses, and sometimes we're only
interested in a fixed number of responses. Let's create a trait for
tracking responses:

    trait ResponseTracker[T] {
      def addResponse(response: T): ResponseTracker[T]
      def isDone: Boolean
    }

The trait `ResponseTracker` models the state of collected responses. The
`addResponse` method is used for advancing the tracking, while `isDone`
is used to detect when enough responses have been collected. Let's see
this in action:

    object Countdown {
      def apply(expectedMessagesCount: Int): Countdown = new Countdown(expectedMessagesCount)
    }

    class Countdown(expectedMessagesCount: Int) extends ResponseTracker[Any] {
      require(expectedMessagesCount >= 0)

      def isDone: Boolean = expectedMessagesCount == 0

      def addResponse(response: Any): Countdown =
        new Countdown((expectedMessagesCount - 1) max 0)
    }

Here the `Countdown` is used for tracking the number of responses that
been received. The tracker is initialised with the number of expected
messages. When the number of expected messages have been received, the
tracker will report as being done. Tracking the number of responses
could be used for getting a rough sample of responses. Here's another
example:

    object MatchIds {
      def apply[Msg, Id](expectedIds: Set[Id], toId: Msg => Id): MatchIds[Msg, Id] =
        new MatchIds(expectedIds, toId)
    }

    class MatchIds[Msg, Id](expectedIds: Set[Id], toId: Msg => Id)
      extends ResponseTracker[Msg] {

      def isDone: Boolean = expectedIds.isEmpty

      def addResponse(response: Msg): MatchIds[Msg, Id] =
        new MatchIds(expectedIds - toId(response), toId)
    }

Here the `MatchIds` is used for tracking IDs in certain responses. The
tracker is initialised with a set of expected IDs and a function for
extracting an ID from an incoming response. When a response is received,
the ID of the response is removed from the expected IDs. When there are
no more expected IDs, the tracker will report as being done.

### Delivering results and timing out

The collected results can be provided to actors using actor messaging,
but another delivery mechanism is needed for code that doesn't read
messages from an actor mailbox.

The collected results can be made available using `Future` values. A
future value represents a placeholder for a value that is yet to be
completed. In our case, the value is the sequence of all the collected
results: `Future[Iterable[T]]` where `T` is the type of the expected
results. Alternatively, if there's an error in the system, the future
can be completed with an exception.

Sometimes everything doesn't go as planned, and some of the responses
never arrive to the response collector. It's good to have a backup
plan: stop waiting for responses after a certain amount time has passed.
The timeout can be provided to the response collector using Scala's
`FiniteDuration`.

It's also a good idea to convey the timeout event to the consumer of
the results. This can be achieved by completing the results with a
timeout error. However, it may be useful to be able to inspect what
values were collected before the timeout was reached. Thus, it's a good
idea to represent the partial results in the type level:

    sealed trait ResultState
    case object Full extends ResultState
    case object Partial extends ResultState

    case class Result[T](values: Iterable[T], state: ResultState)

The type `Result[T]` contains the collection of all the received
responses and a value that tells whether all of the responses were
received or not. The type parameter `T` is the type of the expected
results.

Response collector implementation
---------------------------------

Using the APIs described above, we can create an implementation of the
response collector. We'll start by introducing a new actor,
`ResponseCollector`:

    import akka.actor._
    import scala.concurrent.duration._
    import scala.concurrent.Promise

    class ResponseCollector[T](
      timeout: FiniteDuration,
      initialTracker: ResponseTracker[T],
      result: Promise[Result[T]],
      matcher: PartialFunction[Any, T])
      extends Actor with ActorLogging {

      // ...
    }

The `ResponseCollector` has a type parameter `T` that represents the
type of the collected responses. The actor is initialised with:

-   a timeout duration after which the actor stops listening for
    responses.
-   an initial tracker that is used for detecting when collection is
    finished.
-   a value that can be fulfilled once (`Promise`) with the collected
    results. The `Promise` can be provided to the consumer as a
    `Future`.
-   a matcher that is used for selecting only the interesting responses
    from the incoming messages.

Inside the class a timeout event is set to be triggered after the given
timeout duration has passed. This is done by scheduling a message
delivery to the same actor. The `ReceiveTimeout` message from package
`akka.actor` is used as the timeout message.

    import context.dispatcher

    private val scheduledTimeout = context.system.scheduler.scheduleOnce(
      timeout, self, ReceiveTimeout
    )

The actor has a custom message handler that has state. The state
contains a collection of received responses, and the current state of
the response tracker.

In this handler, only the messages matched by the `matcher` are
selected. On match, the response is appended to the collection of
already collected responses. The handler also advances the response
tracker, and checks whether enough responses were collected. If the
tracker reports as being done, the collected results are reported back
to the consumer using the `Promise` passed in the actor constructor, and
the actor stops itself. If there are still more responses to be
collected, the actor replaces its message handler to include the updated
responses and response tracker.

    private def ready(responses: Vector[T], tracker: ResponseTracker[T]): Receive = {
      case m if matcher.isDefinedAt(m) =>
        val response = matcher(m)
        val nextResponses = responses :+ response
        val nextTracker = tracker.addResponse(response)

        if (nextTracker.isDone) {
          log.info("All responses received.")
          result.success(Result(nextResponses, Full))
          context.stop(self)
        } else {
          context.become(ready(nextResponses, nextTracker))
        }

      case ReceiveTimeout =>
        log.warning("Response collection timed out")
        result.success(Result(responses, Partial))
        context.stop(self)

      case m =>
        log.warning("Unknown message: {}", m)
    }

Besides handling the incoming responses, the handler also handles the
timeout event. When the actor receives `ReceiveTimeout`, it will report
the collected results back to the consumer as a partial result, and
shutdown the actor.

Finally, the actor is set up with the custom message handler with an
initial state as the actor's first message handler.

    def receive: Receive = ready(Vector.empty, initialTracker)

In addition to the actor, we can also provide helpful functions for
creating instances of the actor. The `props` function is used for
creating the `Props` recipe for the actor. The `apply` function creates
the response collector, and exposes only the collected results as a
`Future` and the reference of the actor. In both cases, the timeout is
passed as an implicit parameter in a similar style as it's passed when
using the ask pattern. For `apply` we also pass an `ActorRefFactory`
implicitly, which can be, for example, an actor system or the context of
an actor.

    import akka.util.Timeout

    object ResponseCollector {
      def props[T](
        tracker: ResponseTracker[T],
        result: Promise[Result[T]],
        matcher: PartialFunction[Any, T])
        (implicit timeout: Timeout): Props =
          Props(new ResponseCollector(timeout.duration, tracker, result, matcher))

      def apply[T](tracker: ResponseTracker[T], matcher: PartialFunction[Any, T])
                  (implicit timeout: Timeout, factory: ActorRefFactory) = {
        val result = Promise[Result[T]]()
        val ref = factory.actorOf(props(tracker, result, matcher))
        (result.future, ref)
      }
    }

Example
-------

Let's create a small example demonstrating the use of
`ResponseCollector`. In this scenario, we have some data distributed
among actors. To keep this example simple, our data set is represented
by an in-memory hash map. Our `DataStore` actor serves data from a map
to anyone who requests it by key:

    case class Data(id: String, contents: String) {
      override def toString: String = s"[$id: $contents]"
    }

    object DataStore {
      def props(id: String, items: Map[Int, String]) = Props(new DataStore(id, items))
    }

    class DataStore(id: String, items: Map[Int, String]) extends Actor {
      def receive: Receive = {
        case i: Int => items.get(i).foreach(v => sender() ! Data(id, v))
      }
    }

The `DataStore` also sends an ID specific to the store along with the
data from the map. The ID can be used for identifying the source of the
data. The `DataStore` will also ignore any requests where key is not
found from the map.

For this example, we'll set up three different data stores. All of the
stores contain some information for keys 1-3 except the last one.

    implicit val system = ActorSystem()

    val allStores = List(
      system.actorOf(DataStore.props(
        "name",
        Map(1 -> "Mike", 2 -> "Robert", 3 -> "Joe")
      )),
      system.actorOf(DataStore.props(
        "location",
        Map(1 -> "UK", 2 -> "Sweden", 3 -> "Germany")
      )),
      system.actorOf(DataStore.props(
        "lastPurchase",
        Map(1 -> "couch", 2 -> "laptop")
      ))
    )

For each key, we'll set up a response collector that collects data from
each data store. The collectors are only interested in `Data` objects,
so we'll adjust their matchers accordingly:

    val matcher: PartialFunction[Any, Data] = { case d: Data => d }

In order to know when the collectors can stop collecting, we'll use
`MatchIds` and the IDs from the incoming data to track which data stores
have responded.

    def getId(e: Data): String = e.id
    val tracker = MatchIds(Set("name", "location", "lastPurchase"), getId)

Using the matcher and the tracker we can now set up the response
collectors. As a result, we receive a `Future` value for the collector
results and an actor reference to the collector. We'll pass the timeout
as an implicit parameter to the collectors.

    implicit val timeout = Timeout(1.second)
    val (result1, collector1) = ResponseCollector(tracker, matcher)
    val (result2, collector2) = ResponseCollector(tracker, matcher)
    val (result3, collector3) = ResponseCollector(tracker, matcher)

We can use the collector actor references as the sender when requesting
data from the data stores, so that the responses will be sent to the
collectors.

    allStores.foreach { store =>
      store.tell(1, collector1)
      store.tell(2, collector2)
      store.tell(3, collector3)
    }

Finally, we'll serve the results to the user using the `Future` values
we received earlier. For this example we'll just print the results:

    def printResult(r: Result[Data]): Unit = {
      val status = r.state match {
        case Full => "All values received"
        case Partial => "Only some values received"
      }
      val values = r.values.mkString(", ")
      println(s"$status: $values")
    }

    implicit val ec = system.dispatcher

    result1.foreach(printResult)
    result2.foreach(printResult)
    result3.foreach(printResult)

Running this example will print out the results for all three
collectors. The first two results will print out the full results. Since
we didn't specify the `lastPurchase` information for the third key, the
third collector will timeout after one second, and only partial results
will be printed out.

    All values received: [lastPurchase: laptop], [name: Robert], [location: Sweden]
    All values received: [lastPurchase: couch], [name: Mike], [location: UK]
    Only some values received: [name: Joe], [location: Germany]

Conclusions
-----------

In this article, I've demonstrated how to implement a reusable response
collector actor. The solution allows customisation of how the actor
determines when it has received enough responses, how it determines
which responses are relevant, and how long it will wait for responses
before timing out and delivering partial results. I also showed an
example of how to use the actor and how it behaves.

There's still room for customisation and improvements to the actor. For
example, the timeout mechanism could be customised to reset after each
collected response, error handling could be added, and the responses
could be delivered as a stream instead of a batch.

I've made the code available in a [Github
Gist](https://gist.github.com/jkpl/0c1a70e642be0b2422e3). Feel free to
extend the code as you wish.

Thanks for reading, and happy hAkking!
