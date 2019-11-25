class ActorQueueTest extends TestSpec {
  "ActorQueue" should "enqueue and dequeue an item" in withTestProbe(ActorQueue.props) { (send, tp, _) ⇒
    send(Enqueue(1))
    tp.expectNoMsg(100.millis)
    send(Dequeue)
    tp.expectMsg(1)
    tp.expectNoMsg(100.millis)
  }

  it should "stash a dequeue message when the actor is empty" in withTestProbe(ActorQueue.props) { (send, tp, _) ⇒
    send(Dequeue) // stash this message because the actor starts empty
    tp.expectNoMsg(100.millis)
    send(Enqueue(1))
    tp.expectMsg(1) // should immediately be dequeued because of the unstashed Dequeue message
    tp.expectNoMsg(100.millis)
  }
}