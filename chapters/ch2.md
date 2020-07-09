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

- The [actor model](https://www.brianstorti.com/the-actor-model/): it is a way of thinking of the world that identifies each thing as an actor
    - Send messages
        - Message driven
    - Create new actors
        - Supervision
    - Decide how to behave when it receives its next message
        - This means that actors are stateful, a bit like objects in imperative object-oriented programming (as in Java, Python, and so on).

- Vote-writing actor hierarchy
    ![](https://i.imgur.com/0ulqFUy.png)

- Ensuring resilience with Akka

```scala
/*
An unreliable database
*/
class DatabaseConnection(url: String) {
  /*
  Mutable objects are used very infrequently in idiomatic Scala code.
  In this example, the use of mutability is just to simplify this example.
  It’s not necessary to use mutability here, and we’ll generally avoid the use
  of var in this book in favor of immutable objects. But Scala gives us the choice
  between using mutable and immutable data, which can be helpful for exploring
  the trade-offs of the relevant design choices.
  */
  var votes = new mutable.HashMap[String, Any]()

  def insert(updateMap: Map[String, Any]) = {
    if (Random.nextBoolean()) throw new Exception

    updateMap.foreach {
      case (key, value) => votes.update(key, value)
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////
// Start Using Akka From Here
/////////////////////////////////////////////////////////////////////////////////

/*
A vote case class

Case classes are good for modeling immutable data.
https://docs.scala-lang.org/tour/case-classes.html
*/
case class Vote(timestamp: Long, voterId: Long, howler: String)

/*
A vote-writing actor
*/
class VoteWriter(connection: DatabaseConnection) extends Actor {
  def receive = {
    case Vote(timestamp, voterId, howler) =>
      connection.insert(Map("timestamp" -> timestamp,
        "voterId" -> voterId,
        "howler" -> howler))
  }
}

/*
A supervisory actor

This supervisory actor should merely recover from errors and
not worry about any data that might have been lost.
*/
class WriterSupervisor(writerProps: Props) extends Actor {
  override def supervisorStrategy = OneForeOneStrategy() {
    // Restart: an Akka Directive, a convenient building block provided by the Akka toolkit
    case exception: Exception => Restart
  }

  val writer = context.actorOf(writerProps)

  // Passes all messages through to the supervised VoteWriter actor
  def receive = {
    case message => writer forward message
  }
}

/*
Full voting app

This approach definitely achieves some resilience in the face of failure.
Moreover, it shows how you can build the possibility of failure into the
application structure from the most trivial of beginnings.
But you’re probably not satisfied with this solution:
1. The database uses mutability.
2. The app can and does lose data.
3. Explicitly building this actor hierarchy required you to think a lot about exactly
how the underlying database might fail.
4. Recording data in a database sounds like a common problem that someone else
has probably already solved.

If you see these issues as deficiencies in this design, you’re 100% right.
This simple example isn’t the ideal way to record your data in a database.
All of chapter 3 is dedicated to a better approach to collecting and persisting data. 
*/
object Voting extends App {
  val system = ActorSystem("voting")

  val connection = new DatabaseConnection("http://remotedatabase")
  val writerProps = Props(new VoteWriter(connection))
  val writerSuperProps = Props(new WriterSupervisor(writerProps))

  // Creates actors from the Props objects
  val votingSystem = system.actorOf(writerSuperProps)

  // Sending messages in Akka is done using the ! method.
  votingSystem ! Vote(1, 5, "nom nom")
  votingSystem ! Vote(2, 7, "Mikey")
  votingSystem ! Vote(3, 9, "nom nom")

  println(connection.votes)
}
```

### Spark, a reactive big data framework

- Spark is a framework for large-scale data processing, written in Scala.
    - It’s an incredibly fast data-processing engine.
    - It can handle enormous amounts of data when used on a cluster.
    - It’s easy to use, thanks to an elegant, functional API.
    - It has libraries that support common use cases like data analysis, graph analytics, and machine learning.
- The Spark framework is one of several high-level tools that we’ll use in this book to build reactive machine learning systems. In this example, we’ll tackle the problem of predicting the number of votes that the Howlywood Star systems will receive in the hours leading up to the close of voting.
- In this example, we’ll tackle the problem of predicting the number of votes that the Howlywood Star systems will receive in the hours leading up to the close of voting.

```scala
/*
Basic Spark setup

The SparkSession object you create here is used as your connection between regular Scala
code and objects managed by Spark, potentially on a cluster
*/
val session = SparkSession.builder.appName("Simple ModelExample").getOrCreate()
import session.implicits._ // imports serializers for primitive datatypes in session

/*
Handling the data path
*/
val inputBasePath = "example_data"
val outputBasePath = "."
val trainingDataPath = inputBasePath + "/training.txt"
val testingDataPath = inputBasePath + "/testing.txt"
val currentOutputPath = outputBasePath + System.currentTimeMillis()

/*
Loading training and testing data

Next, you need to load these files into Spark’s in-memory representation of
datasets, called resilient distributed datasets (RDDs). RDDs are Spark’s core
abstraction. They provide enormous amounts of data in memory, spread across a 
cluster without you having to explicitly implement that distribution. In fact, 
RDDs can even handle what happens when some data disappears due to cluster nodes 
failing, again without you having to concern yourself with handling this failure.

- higer-order function
  - Spark will literally serialize this function and send it to all the nodes storing
    the data in the RDD. Shifting from sending data to functions to sending functions
    to data is one of the changes you make when using big data stacks like Hadoop and Spark.
- lazy
  - No data is read at the time that these commands are issued
*/
val trainingData = session.read.textFile(trainingDataPath)
val trainingParsed = trainingData.map { line =>
  val parts = line.split(',')
  // A LabeledPoint is a machine learning specific type designed to encode an instance
  // consisting of a feature vector and a concept.
  LabeldPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}.cache() // The cache method tells Spark you intend to reuse this data, so keep it in memory if possible.

val testingData = session.read.textFile(testingDataPath)
val testingParsed = testingData.map {
  val parts = line.split(',')
  LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}.cache()

/*
Training a model

Using linear regression
At this point, you’ve merely learned a model—you have no idea whether that model is good enough for the
critical problem of predicting future canine singing sensations.
*/
val numIterations = 100
val model = LinearRegressionWithSGD.train(trainingParsed.rdd, numIterations)

/*
Testing a model

To understand how useful this model is, you should now evaluate it on data that it hasn’t seen - the testing set.
*/
val predictionsAndLabels = testingParsed.map {
  case LabeledPoint(label, features) => val prediction = model.predict(features)
  (prediction, label)
}

/*
Model metrics
*/
val metrics = new MulticlassMetrics(predictionsAndLabels.rdd)
val precision = metrics.precision
val recall = metrics.recall
println(s"Precision: $precision Recall: $recall")

/*
Saving a  model
*/
model.save(session.SparkContext, currentOutputPath)
```