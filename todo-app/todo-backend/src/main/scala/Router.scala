import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.Await

trait Router {
  def route: Route
}

class AppRouter(todoStorage: TodoStorage) extends Router with Directives {

  import io.circe.generic.auto._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  override def route: Route = pathPrefix("todos") {
    pathEndOrSingleSlash {
      get {
        //var html = Await.result(get("http://akka.io"), 10.seconds)
        //println(html)
        complete(todoStorage.all())
      }
    } ~ path("done") {
      get{
        complete(todoStorage.done())
      }
    } ~ path("pending") {
      complete(todoStorage.pending())
    }
  }
}
