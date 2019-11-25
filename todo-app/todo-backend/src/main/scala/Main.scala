import akka.actor.ActorSystem

import scala.util.{Failure, Success}
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App{

  val host = "127.0.0.1"
  val port = 8080
  implicit val system = ActorSystem(name="todoApp")
  implicit val actorMaterializer = ActorMaterializer()
  import system.dispatcher

  val todoStorage = new InMemoryTodoDB(Seq(
    Todo("0", "Make Hello World Todo App", "Make starter code", true),
    Todo("1", "Complete Basic Todo App", "Add Routes and simple DB support", false),
    Todo("2", "Extend Todo App", "Add support for Post and Update", false)
  ))

  val router = new AppRouter(todoStorage)
  val server = new Server(router, host, port)

  val bindServer = server.bind()
  bindServer.onComplete({
    case Success(_) => println(s"Server is listening on $host:$port")
    case Failure(exception) => println(s"Failed to bind server with error: ${exception.getMessage}")
  })

  Await.result(bindServer, 3.seconds)

}
