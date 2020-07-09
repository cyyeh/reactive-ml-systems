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