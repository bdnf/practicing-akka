package helloAkka

import akka.actor.{Actor, ActorSystem, Props}
import supervision.MasterWorker.system

case class Greeting(who: String)
case class WordCount(words: String)

class SimpleApp extends Actor {
  override def receive: Receive = {
    case Greeting(who) => println(s"Hello $who")
    case WordCount(words) => words
      .toLowerCase
      .replaceAll("""[^\w\s\.\$]""", "")
      .split(" ")
      .groupBy((word:String) => word)
      .mapValues(_.length)
      .foreach(println)
  }
}

object SimpleAppDemo extends App {

  val system = ActorSystem("Hello-World")

  val greeter = system.actorOf(Props[SimpleApp], "simple-app")

  greeter ! Greeting("Akka")

  greeter ! WordCount("Please, count appearances and number of unique words in this string!")

  system.terminate()
}
