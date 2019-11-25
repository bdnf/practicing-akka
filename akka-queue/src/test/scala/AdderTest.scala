import akka.actor.Props
import akka.testkit.TestProbe
import code.ActorQueue.Dequeue

import scala.concurrent.duration._

class ConsumerActorTest extends TestSpec {

  "consumer actor" should "send enqueue to the queue upon receiving 'start'" in withConsumerActorProps { (props, queue) ⇒
    withTestProbe(props) { (send, tp, _) ⇒
      send("start")
      tp.expectNoMsg(100.millis)
      queue.expectMsg(Dequeue)
      queue.expectNoMsg(100.millis)
    }
  }

  it should "send a dequeue message to the queue, upon receiving 1000 integers" in withConsumerActorProps { (props, queue) ⇒
    withTestProbe(props) { (send, tp, consumerActor) ⇒
      (1 to 1000).foreach(send(_))
      tp.expectNoMsg(100.millis)
      queue.expectMsgAllOf((1 to 999).map(_ ⇒ Dequeue): _*)
      queue.expectNoMsg(100.millis)
      tp watch consumerActor // the test probe must watch the actor to receive actor life cycle messages
      tp.expectTerminated(consumerActor) // when the consumer actor has been terminated, the 'Terminated' message will be expected
    }
  }

  /**
    * Initializes the ConsumerActor's props with the actor ref of the test probe,
    * that will act as the 'queue'. It will receive the messages that the consumer
    * will send.
    */
  def withConsumerActorProps(f: (Props, TestProbe) ⇒ Unit): Unit = {
    val tp = TestProbe()
    f(ConsumerActor.props(tp.ref), tp)
  }
}import code.Adder.{ Add, GetValue, Subtract }

import scala.concurrent.duration._

class AdderTest extends TestSpec {
  "Adder" should "start with 0" in withTestProbe(Adder.props) { (send, tp, _) ⇒
    send(GetValue)
    tp.expectMsg(0)
  }

  it should "Increment value with 1 on receive of Add" in withTestProbe(Adder.props) { (send, tp, _) ⇒
    send(Add)
    tp.expectNoMsg(100.millis)
    send(GetValue)
    tp.expectMsg(1)
  }

  it should "increment to 10" in withTestProbe(Adder.props) { (send, tp, _) ⇒
    (1 to 10).foreach(_ ⇒ send(Add))
    tp.expectNoMsg(100.millis)
    send(GetValue)
    tp.expectMsg(10)
  }

  it should "increment to 10 and decrement to 0" in withTestProbe(Adder.props) { (send, tp, _) ⇒
    (1 to 10).foreach(_ ⇒ send(Add))
    tp.expectNoMsg(100.millis)
    (1 to 10).foreach(_ ⇒ send(Subtract))
    tp.expectNoMsg(100.millis)
    send(GetValue)
    tp.expectMsg(0)
  }
}