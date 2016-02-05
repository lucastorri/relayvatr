package relayvatr.scheduler

import relayvatr.control._
import relayvatr.event._
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable

class SyncFCFSScheduler(config: ControlSystemConfig) extends SystemScheduler {

  private val free = mutable.HashSet((1 to config.elevators).map(n => s"elevator-$n"): _*)
  private val inUse = mutable.HashSet.empty[String]
  private val waiting = mutable.ListBuffer.empty[Call]

  private val subject = Subject[ActionEvent]()

  override def events: Observable[ActionEvent] = subject

  override def shutdown(): Unit = subject.onCompleted()

  override def handle(action: Action): Unit = synchronized { act(action) }

  private val act: PartialFunction[Action, Unit] = {
    case call @ Call(id, floor, direction) =>
      lockAndGet() match {
        case Some(elevatorId) => subject.onNext(ActionEvent(call, ElevatorArrived(elevatorId, floor)))
        case None => waiting += call
      }
    case goto @ GoTo(id, elevatorId, floor) =>
      subject.onNext(ActionEvent(goto, ElevatorArrived(elevatorId, floor)))
      if (waiting.isEmpty) {
        unlock(elevatorId)
      } else {
        val nextCall = waiting.remove(0)
        subject.onNext(ActionEvent(nextCall, ElevatorArrived(elevatorId, nextCall.floor)))
      }
  }

  private def lockAndGet(): Option[String] = {
    val first = free.headOption
    first.foreach { elevatorId =>
      free.remove(elevatorId)
      inUse.add(elevatorId)
    }
    first
  }

  private def unlock(elevatorId: String): Unit = {
    inUse.remove(elevatorId)
    free.add(elevatorId)
  }

}
