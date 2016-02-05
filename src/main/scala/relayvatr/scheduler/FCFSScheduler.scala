package relayvatr.scheduler

import relayvatr.control.ControlSystemConfig
import relayvatr.event._

import scala.collection.mutable

trait FCFSScheduler extends SystemScheduler with EventSubject {

  def config: ControlSystemConfig

  private val free = mutable.HashSet((1 to config.elevators).map(n => s"elevator-$n"): _*)
  private val inUse = mutable.HashSet.empty[String]
  private val waiting = mutable.ListBuffer.empty[Call]

  protected val act: PartialFunction[Action, Unit] = {
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
