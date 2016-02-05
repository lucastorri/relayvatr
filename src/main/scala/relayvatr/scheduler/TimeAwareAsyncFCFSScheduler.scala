package relayvatr.scheduler

import akka.actor.{Actor, ActorSystem, Props}
import relayvatr.control.ControlConfig
import relayvatr.event._

import scala.collection.mutable
import scala.concurrent.duration._

class TimeAwareAsyncFCFSScheduler(config: ControlConfig)(implicit system: ActorSystem) extends Scheduler with EventSubject {

  import system.dispatcher

  private val handler = system.actorOf(Props(new Handler))
  private val awaiting = mutable.ListBuffer.empty[Call]
  private val elevators = (1 to config.elevators).map { i =>
    val id = s"elevator-$i"
    id -> new Elevator(id)
  }.toMap

  override def handle(action: Action): Unit = handler ! action

  override def shutdown(): Unit = {
    system.stop(handler)
    subject.onCompleted()
  }

  private class Elevator(val id: String) {

    var isFree: Boolean = true
    var currentFloor: Int = 0

    def travelTimeTo(floor: Int): FiniteDuration =
      config.travelTimePerFloor * math.abs(currentFloor - floor)

  }

  private class Handler extends Actor {

    override def receive: Receive = {
      case a: Action => act(a)
      case ae: ActionEvent => subject.onNext(ae)
    }

    val act: PartialFunction[Action, Unit] = {

      case call: Call if isInvalidFloor(call.floor) =>
        subject.onNext(ActionEvent(call, InvalidFloor(call.floor)))

      case goto: GoTo if isInvalidFloor(goto.floor) =>
        subject.onNext(ActionEvent(goto, InvalidFloor(goto.floor)))

      case call: Call =>
        lockAndGetFreeElevator match {
          case Some(elevator) => goToUser(call, elevator)
          case None => addToQueue(call)
        }

      case goto: GoTo =>
        val elevator = elevators(goto.elevatorId)
        after(elevator.travelTimeTo(goto.floor)) {
          if (awaiting.isEmpty) releaseElevator(elevator, goto.floor)
          else goToUser(awaiting.remove(0), elevator)
          ActionEvent(goto, ElevatorArrived(elevator.id, goto.floor))
        }

    }

    def goToUser(call: Call, elevator: Elevator): Unit = {
      after(elevator.travelTimeTo(call.floor)) {
        elevator.currentFloor = call.floor
        ActionEvent(call, ElevatorArrived(elevator.id, call.floor))
      }
    }

    def after(duration: FiniteDuration)(ae: => ActionEvent): Unit =
      context.system.scheduler.scheduleOnce(duration) {
        self ! ae
      }

  }

  private def isInvalidFloor(floor: Int): Boolean =
    !config.limit.canGoTo(floor)

  private def addToQueue(call: Call): Unit =
    awaiting += call

  private def releaseElevator(elevator: Elevator, floor: Int): Unit = {
    elevator.isFree = true
    elevator.currentFloor = floor
  }

  private def lockAndGetFreeElevator: Option[Elevator] = {
    val free = elevators
      .find { case (_, elevator) => elevator.isFree }
      .map { case (_, elevator) => elevator }

    free.foreach(_.isFree = false)
    free
  }

}
