package creatingChildren
import akka.actor.{Actor, ActorRef, Props}

object ParentChildren extends App {

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(ref: ActorRef): Receive = {
      case TellChild(message) => ref forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case msg => println(s"${self.path} received a message $msg")
    }
  }
}
