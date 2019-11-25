import scala.concurrent.{ExecutionContext, Future}

trait TodoStorage {
  def pending(): Future[Seq[Todo]]

  def done(): Future[Seq[Todo]]

  def all(): Future[Seq[Todo]]

}

class InMemoryTodoDB(todos: Seq[Todo] = Seq.empty)(implicit ex: ExecutionContext) extends TodoStorage {
  override def pending(): Future[Seq[Todo]] = Future.successful(todos)

  override def done(): Future[Seq[Todo]] = Future.successful(todos.filter(_.done))

  override def all(): Future[Seq[Todo]] = Future.successful(todos.filterNot(_.done))
}
