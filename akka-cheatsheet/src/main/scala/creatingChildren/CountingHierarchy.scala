package creatingChildren

import akka.actor.{Actor, ActorRef, ActorSystem, Identify, Props}
import creatingChildren.RootActor._
import creatingChildren.WorkerNode._

import scala.collection.{immutable, mutable}

object RootActor {
  case class CombiningTree(width: Int)
  case class CreateHierarchy(newWidth: Int, allowedLevels: Int)
  case class Multiply(newWidth: Int, allowedLevels: Int)
  case class ChildResponse(createdChildrenNum: Int)
  case class BuildingComplete(level: Int)
}

class RootActor extends Actor {

  override def receive: Receive = withChildren(IndexedSeq(), 0, 0)

  def withChildren(refs: IndexedSeq[ActorRef], newTotal: Int, allowedLevels: Int): Receive = {
    case CombiningTree(width) =>
      val log2 = (x: Double) => math.log10(x)/math.log10(2.0)
      val numOfLevels = log2(width).toInt
      println(s"Initializing Combining Tree...with maximum number of levels: ${numOfLevels}")
      val selfChildren = 2
      val childrenRefs = for (i <- 1 to selfChildren) yield context.actorOf(Props[WorkerNode], s"Node$i")

      for ( ref <- childrenRefs) {
        println(s"Forwarding msg to ${ref.path.name}")
        //sender os root
        //ref ! CreateHierarchy(width - 1 - selfChildren, numOfLevels - 1)
        ref ! Multiply(width - 1 - selfChildren, numOfLevels - 1)
      }
      context.become(withChildren(childrenRefs, width, numOfLevels))
    case ChildResponse(createdChildrenNum) =>
      println(s"Received update status from Node: Nodes created $createdChildrenNum")
      //context.become(withChildren(refs, newTotal - createdChildrenNum, allowedLevels))
    case BuildingComplete(level) =>
      println(s"Children responded with completion of Building level $level!       ${self.path.name}")
  }
}

object WorkerNode {
  sealed trait Status
  case object IDLE extends Status
  case class FIRST(value: Int) extends Status
  case class SECOND(value: Int) extends Status
  case object DONE extends Status

  def evaluateTask(): Int = return 1

  def addRefsToMap(map: mutable.Map[Int, IndexedSeq[ActorRef]], allowedLevels: Int, childrenRefs: IndexedSeq[ActorRef]) = {
    var actorMap = map
    actorMap.contains(allowedLevels) match {
      case true => {
        val refs = actorMap(allowedLevels) ++ childrenRefs
        actorMap += (allowedLevels -> refs)
      }
      case false => actorMap += (allowedLevels -> childrenRefs)
    }
    //for ((k,v) <- actorMap) print(k + ":" + v)
    actorMap
  }
}

class WorkerNode extends Actor {

  override def receive: Receive = withMoreChildren(mutable.Map(), IndexedSeq(), 0,0)

  def withMoreChildren(actorMap:mutable.Map[Int, IndexedSeq[ActorRef]], childrenRefs: IndexedSeq[ActorRef], newTotal: Int, allowedLevels: Int): Receive = {
    case Multiply(newTotal, allowedLevels) =>
      //populate self
      context.self ! CreateHierarchy(newTotal, allowedLevels)
    case BuildingComplete(level) =>
      println(s"SubChild responded with completion of Building level $level       ${sender.path.name} to ${self.path.name}")
      actorMap map ({case(k,v) => println( k + ":" + v)})
      actorMap.get(level + 1) match {
        case Some(actors) => {
          println(s"Level ${level+1} contains -> ${
            actors foreach( x => print(x + " "))
          }")
          for (a <- actors) yield a ! BuildingComplete(level + 1)
        }
        case None => {
          println("Level does not exist")
          context.parent ! BuildingComplete(level + 1)
        }
      }
      context.parent ! BuildingComplete(level + 1)
    case CreateHierarchy(newTotal, allowedLevels) =>
      println(s"${self.path.name} received Request for creating $newTotal Children and $allowedLevels tree levels to create")

      val selfChildren = 2
      val newDifference = newTotal - selfChildren
      if (newDifference == 1) {
        var node = context.actorOf(Props[WorkerNode], s"LeafOf_${sender.path.name}_last")
        val newActorMap = WorkerNode.addRefsToMap(actorMap, allowedLevels, IndexedSeq(node))
        sender ! BuildingComplete(allowedLevels)
      }
      else if (newDifference >= 0) {
        if (allowedLevels <= 0){ //stop, but
          println("This level contains Leaves")

          val subChildrenRefs = for (i <- 1 to selfChildren) yield context.actorOf(Props[WorkerNode], s"LeafOf_${sender.path.name}_$i")

          for (child <- subChildrenRefs) {
            sender ! BuildingComplete(allowedLevels)
          }

          val newActorMap = WorkerNode.addRefsToMap(actorMap, allowedLevels, subChildrenRefs)

        } else {


          val childrenRefs = for (i <- 1 to selfChildren) yield context.actorOf(Props[WorkerNode], s"childOf${sender.path.name}_$i")

          for (ref <- childrenRefs) {
            println(s"Multiplying self. Actor is: ${ref.path.name}")
            //ref ! CreateHierarchy(newDifference, allowedLevels - 1)
            ref ! Multiply(newDifference, allowedLevels - 1)
          }


          var newActorMap = WorkerNode.addRefsToMap(actorMap, allowedLevels, childrenRefs)

          context.become(withMoreChildren(newActorMap, childrenRefs, newDifference, allowedLevels - 1))
        }
      }
      else { println(s"ERROR! New difference is: $newDifference" )}
  }

  def withStatus(status: Status, prior: Int): Receive = {
    case DONE =>
      println(s"Sender is: ${sender()}")
      println(s"Parent is: ${context.parent}")

    case IDLE =>
      val firstValue = WorkerNode.evaluateTask()
      context.become(withStatus(FIRST(firstValue), prior))
    case FIRST(firstValue) =>
      context.become(withStatus(SECOND(prior + firstValue), prior + firstValue))
    case SECOND(result) => context.become(withStatus(DONE, result))

  }
}

object CountingHierarchy extends App {
  val parallelismLevel = 10 //number of threads in parallel programming

  val system = ActorSystem("CountingTreeHiererchy")
  val rootActor = system.actorOf(Props[RootActor], "rootCountingActor")
  rootActor ! CombiningTree(width = parallelismLevel)

  Thread.sleep(5000)
  system.terminate()
}
