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