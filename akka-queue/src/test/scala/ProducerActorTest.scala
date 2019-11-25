import akka.actor.Props
import akka.testkit.TestProbe
import ActorQueue.Enqueue
import scala.concurrent.duration._

class ProducerActorTest extends TestSpec {
  "producer actor" should "produce 1000 enqueue messages upon receiving 'start'" in withProducerActorProps { (props, consumer) ⇒
    withTestProbe(props) { (send, tp, _) ⇒
      send("start")
      tp.expectNoMessage(100.millis)
      consumer.expectMsgAllOf((1 to 1000).map(Enqueue(_)): _*) // notice, all messages are received in order
      consumer.expectNoMessage(100.millis)
    }
  }

  it should "do nothing on receiving an unknown message" in withProducerActorProps { (props, consumer) ⇒
    withTestProbe(props) { (send, tp, _) ⇒
      send("unknown message")
      tp.expectNoMessage(100.millis)
      consumer.expectNoMessage(100.millis)
    }
  }

  /**
   * Initializes the ProducerActor's props with the actor ref of the test probe,
   * that will act as a 'consumer'. It will receive the messages that the producer
   * will send.
   */
  def withProducerActorProps(f: (Props, TestProbe) ⇒ Unit): Unit = {
    val tp = TestProbe()
    f(ProducerActor.props(tp.ref), tp)
  }
}