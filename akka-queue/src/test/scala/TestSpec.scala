import akka.actor.Actor.Receive
import akka.actor.{ Actor, ActorRef, ActorSystem, PoisonPill, Props }
import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers }

import scala.concurrent.duration._

class TestSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {
  implicit val system: ActorSystem = ActorSystem()
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 5.seconds)
  implicit val timeout = Timeout(5.seconds)

  /**
   * Returns a new test probe
   */
  def testProbe: TestProbe = TestProbe()

  /**
   * Terminates actors
   */
  def terminate(actors: ActorRef*): Unit = {
    val tp = TestProbe()
    actors.foreach { (actor: ActorRef) ⇒
      tp watch actor
      actor ! PoisonPill
      tp expectTerminated actor
    }
  }

  /**
   * Constructs an actor with the given props, initializes a
   * send function to more easily send messages to the actor-under-test,
   * and initializes a test probe. Both the send function and the test probe
   * will be injected in the given function wich will contain the test code.
   * When the function completes, both the test probe and the actor will be
   * terminated.
   */
  def withTestProbe(props: Props)(f: (Any ⇒ Unit, TestProbe, ActorRef) ⇒ Unit): Unit = {
    val ref = system.actorOf(props, props.clazz.getName)
    val tp = TestProbe()
    val sendFunction = tp.send(ref, _: Any)
    try f(sendFunction, tp, ref) finally terminate(ref)
  }

  def ignoringActor: ActorRef = system.actorOf(Props(new Actor {
    override def receive: Receive = Actor.ignoringBehavior
  }))

  override protected def afterAll(): Unit =
    system.terminate().futureValue
}