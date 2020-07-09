# [Chapter 2: Using reactive tools](https://hackmd.io/@distributed-systems-engineering/reactive-ml-sytems-ch2)

###### tags: `ml-systems`

## Outline

- Managing uncertainty with Scala
- Implementing supervision and fault tolerance with Akka
- Using Spark and MLlib as frameworks for distributed machine learning pipelines

## Brief Summary

- Scala gives you constructs to help you reason about uncertainty:
    - `Options` abstract over the uncertainty of something being present or not.
    - `Futures` abstract over the uncertainty of actions, which take time.
    - `Futures` give you the ability to implement timeouts, which help ensure responsiveness through bounding response times.
- With Akka, you can build protections against failure into the structure of your application using the power of the actor model:
    - Communication via message passing helps you keep system components contained.
    - Supervisory hierarchies can help ensure resilience of components.
    - One of the best ways to use the power of the actor model is in libraries that use it behind the scenes, instead of doing much of the definition of the actor system directly in your code.
- Spark gives you reasonable components to build data-processing pipelines:
    - Spark pipelines are constructed using pure functions and immutable transformations.
    - Spark uses laziness to ensure efficient, reliable execution.
    - MLlib provides useful tools for building and evaluating models with a minimum of code.

## Key Points

- Context
    ![](https://i.imgur.com/xcpUSc7.png)

### Scala, a reactive language

- Howlywood Star voting application architecture
    ![](https://i.imgur.com/BrgkLKJ.png)
    - This system is very simple, but even a system as simple as this has hidden complexity. Consider the following questions:
        - How long will it take to record each vote?
        - What will the server be doing while it waits for each vote to be persisted?
        - How can the visualization apps be kept as fresh as possible?
        - What will happen if load increases dramatically?

- Reacting to uncertainty in Scala

```scala
package com.reactivemachinelearning

/*
futures
*/
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random

object Main extends App {
  /*
  A map of votes
  */
  val totalVotes = Map("Mikey" -> 52, "nom nom" -> 105)

  val naiveNomNomVotes: Option[Int] = totalVotes.get("nom nom")
  println(naiveNomNomVotes) // Some(105)

  /*
  Handling no votes using pattern matching

  Pattern matching is a language feature used to encode what the 
  possible values are that a given operation could produce.
  */
  def getVotes(howler: String) = {
    totalVotes.get(howler) match {
      case Some(votes) => votes
      case None => 0
    }
  }

  val nomNomVotes: Int = getVotes("nom nom") // 105
  val indiaVotes: Int = getVotes("Indiana") // 0

  /*
  Setting default values on maps
  */
  val totalVotesWithDefault = Map("Mikey" -> 52, "nom nom" -> 105).withDefaultValue(0)

  /*
  A remote “database”

  This sort of uncertainty is a big problem for the vote visualization app.
  Its server will be doing nothing, just waiting, while that call is processed.
  You can imagine that won’t help in the quest to achieve responsiveness at all times.
  
  The source of this performance problem is that the call to getRemoteVotes is synchronous.
  The solution to this problem is to use a future, which will ensure that this call is
  no longer made in a synchronous, blocking fashion.
  */
  def getRemoteVotes(howler: String) = {
    Thread.sleep(Random.nextInt(1000))
    totalVotesWithDefault(howler)
  }

  val mikeyVotes = getRemoteVotes("Mikey") // always returns 52, eventually

  /*
  Futures-based remote calls

  Using a future, you’ll be able to return immediately from a remote call like
  this and collect the result later, once the call has completed.
  */
  def futureRemoteVotes(howler: String) = Future {
    getRemoteVotes(howler)
  }

  val nomNomFutureVotes = futureRemoteVotes("nom nom")
  val mikeyFutureVotes = futureRemoteVotes("Mikey")
  val indianaFutureVotes = futureRemoteVotes("Indiana")

  val topDogVotes: Future[Int] = for {
    nomNom <- nomNomFutureVotes
    mikey <- mikeyFutureVotes
    indiana <- indianaFutureVotes
  } yield List(nomNom, mikey, indiana).max

  topDogVotes onSuccess {
    case _ => println("The top dog currently has " + topDogVotes + " votes.")
  }

  /*
  Futures-based timeouts

  In this implementation, you accept that life isn’t perfect, and some remote calls
  may exhibit unacceptable latency. Rather than pass that latency on to the user,
  you choose to return a degraded response, the historical average number of votes.
  
  In a real system, you may have several options for what to return as a degraded response.
  For example, you may have another application to look this value up in, such as a cache.
  That cache’s value may have gotten stale, but that degraded value might be more useful than nothing at all.
  In other cases, you may want to encode retry logic. It’s up to you to figure out what’s best for your application.
  */
  val timeoutDuration = 500
  val AverageVotes = 42

  val defaultVotes = Future {
    Thread.sleep(timeoutDuration)
    AverageVotes
  }

  def timeOutVotes(howler: String) = Future.firstCompletedOf(List(futureRemoteVotes(howler), defaultVotes))
  val nomNomTimeoutFutureVotes = timeOutVotes("nom nom")
  nomNomTimeoutFutureVotes onSuccess {
    case _ => println("TimeOutVotes: " + nomNomTimeoutFutureVotes)
  }
}
```

### Akka, a reactive toolkit


### Spark, a reactive big data framework