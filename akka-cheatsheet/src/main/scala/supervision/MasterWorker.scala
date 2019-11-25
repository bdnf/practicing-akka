package supervision
import scala.concurrent.duration._

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}

class Master extends Actor {
  import Worker._

  var childRef: ActorRef = _

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 3 second){
    case ResumeOnException => Resume
    case RestartOnException => Restart
    case StopOnException => Stop
    case _:Exception => Escalate
  }

  override def preStart(): Unit = {
    //create Worker
    childRef = context.actorOf(Props[Worker], "Worker")
    println(s"Worker created by Master. Worker id: $childRef")
  }

  override def receive: Receive = {
    case message => {
      println(s"Message received by Master: $message")
      childRef ! message
      println(s"Redirected message to child $childRef")
      Thread.sleep(2000)
    }
  }

}

object Worker {
  case object ResumeOnException extends Exception
  case object StopOnException extends Exception
  case object RestartOnException extends Exception
}

class Worker extends Actor {
  import Worker._
  override def preStart(): Unit = {
    println("Worker created successfully. Worker doing its preparation")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Exception happened. Calling preRestart hook ... ")
    super.preRestart(reason, message)
  }

  override def postStop(): Unit = {
    println("Worker stopped!")
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Worker restarted successfully")
    super.postRestart(reason)
  }

  override def receive: Receive = {
    case "Resume" => throw ResumeOnException
    case "Stop" => throw StopOnException
    case "Restart" => throw RestartOnException
    case _ => throw new Exception
  }
}

object MasterWorker extends App {

  val system = ActorSystem("supervision-example")
  val master = system.actorOf(Props[Master], "master")

  master ! "Resume"
  Thread.sleep(2000)

  master ! "Restart"
  Thread.sleep(2000)

  master ! "Stop"
  Thread.sleep(2000)

  system.terminate()
}
