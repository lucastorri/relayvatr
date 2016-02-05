package relayvatr.scheduler

import relayvatr.control.ControlConfig
import relayvatr.event._

import scala.collection.mutable

trait FCFSScheduler extends SystemScheduler with EventSubject {

  def config: ControlConfig

  private val free = mutable.HashSet((1 to config.elevators).map(n => s"elevator-$n"): _*)
  private val inUse = mutable.HashSet.empty[String]
  private val waiting = mutable.ListBuffer.empty[Call]

  protected val act: PartialFunction[Action, Unit] = {
    case call: Call if !config.limit.canGoTo(call.floor) =>
      subject.onNext(ActionEvent(call, InvalidFloor(call.floor)))
    case goto @ GoTo(id, elevatorId, floor) if !config.limit.canGoTo(floor) =>
      subject.onNext(ActionEvent(goto, InvalidFloor(goto.floor)))
    case call: Call =>
      lockAndGet() match {
        case Some(elevatorId) => subject.onNext(ActionEvent(call, ElevatorArrived(elevatorId, call.floor)))
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
