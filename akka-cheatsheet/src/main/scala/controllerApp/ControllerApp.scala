package controllerApp

import akka.actor.{Actor, ActorSystem, Props, Stash}
import controllerApp.ControllerTier.{Get, Post}
import controllerApp.FrontendTier.{ShowData, UpdateData}
import controllerApp.DatabaseTier._

object DatabaseTier {
  case object Connect
  case object Disconnect
}

object ControllerTier {
  sealed trait ControllerMsg
  case object Get extends ControllerMsg
  case object Post extends ControllerMsg

  def props = Props[ControllerApp]
}

class ControllerApp extends Actor {
  override def receive: Receive = {
    case Get => println("Backend: Fetching data from the DB")
    case Post => println("Backend: Submitted new data to the DB")
    case _ => println("Request not supported")
  }
}

object FrontendTier {
  sealed trait FrontendMsg
  case object ShowData extends FrontendMsg
  case object UpdateData extends FrontendMsg
}

class FrontendApp extends Actor with Stash{
  // val controller = context.actorOf(Props[ControllerApp], "controller") // BAD
  val controller = context.actorOf(ControllerTier.props, "controller") // GOOD

  def receive = disconnected()

  def disconnected(): Actor.Receive = {
    case Connect =>
      println(s"Controller connected to DB")
      unstashAll()
      context.become(connected)
    case _ =>
      stash()
  }

  def connected(): Actor.Receive = {
    case Disconnect =>
      println("Controller disconnect from DB")
      context.unbecome()
    case ShowData =>
      println(" Showing some data on the screen")
      controller ! Get
    case UpdateData =>
      //println(s"Controller received ${op} from user: ${user}")
      println(" Submitting new data to the app")
      controller ! Post
  }

}

object ControllerAppDemo extends App {

  val system = ActorSystem("WebAppMockup")

  val webapp = system.actorOf(Props[FrontendApp], "simple-web-app")

  webapp ! Connect
  webapp ! ShowData
  Thread.sleep(2000)
  webapp ! UpdateData

  system.terminate()

}
