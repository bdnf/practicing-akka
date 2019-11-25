package supervision

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Terminated}

class Target extends Actor {
  override def receive: Receive = {
    case message => {
      println(s"target received message: $message")
    }
  }
}

class Supervisor(target: ActorRef) extends Actor {

  override def preStart(): Unit = {
    context.watch(target)
  }
  override def postStop(): Unit = {
    println("Nobody to monitor now. Stopping app")
  }
  override def receive: Receive = {
    case Terminated(_) => println("Supervisor: OMG! Target is down")
      context.stop(self)
    case _ => println("Supervisor received a message")
  }
}
object Monitoring extends App {

  val system = ActorSystem("monitoring")

  val target = system.actorOf(Props[Target], "target")

  val monitor = system.actorOf(Props(classOf[Supervisor], target), "monitoring-app")

  target ! "Stop target"
  Thread.sleep(500)
  target ! "Still moving"
  target ! "..."

  target ! PoisonPill

  system.actorSelection("target") ! "Are you still alive?"
  Thread.sleep(2000)

  system.terminate()
}
