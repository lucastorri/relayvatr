package relayvatr.scheduler

import akka.actor.{Actor, ActorSystem, Props}
import relayvatr.control._
import relayvatr.event._
import relayvatr.exception.InvalidFloorException
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable

class ClosestElevatorScheduler(
  config: ControlConfig,
  clock: Observable[Unit],
  elevatorFactory: (String) => ElevatorBehaviour
)(implicit system: ActorSystem) extends Scheduler {

  private val handler = system.actorOf(Props(new Handler))
  private val subject = Subject[ElevatorEvent]()
  private var awaitingCalls = mutable.ListBuffer.empty[Call]
  private val elevators = (1 to config.elevators).map(i => elevatorFactory(s"elevator-$i"))
  private val clockSubscription = clock.subscribe(_ => handler ! 'clock)

  override def events: Observable[ElevatorEvent] = subject

  override def handle(action: Action): Unit = action match {
    case call: Call if isInvalidFloor(call.floor) => throw new InvalidFloorException(call.floor)
    case goto: GoTo if isInvalidFloor(goto.floor) => throw new InvalidFloorException(goto.floor)
    case _ => handler ! action
  }

  override def shutdown(): Unit = {
    clockSubscription.unsubscribe()
    system.stop(handler)
    subject.onCompleted()
  }

  private def isInvalidFloor(floor: Int): Boolean =
    !config.limit.canGoTo(floor)

  private class Handler extends Actor {

    override def receive: Receive = {
      case a: Action =>
        act(a)
      case 'clock =>
        awaitingCalls = awaitingCalls.filterNot(answeredToCall)
        elevators.flatMap(_.move()).foreach(subject.onNext)
    }

    val act: PartialFunction[Action, Unit] = {
      case call: Call =>
        awaitingCalls += call
      case goto: GoTo =>
        elevators.find(_.id == goto.elevatorId).foreach(_.press(goto.floor))
    }

    def answeredToCall(call: Call): Boolean = {
      val (elevator, distance) = elevators
        .map(elevator => elevator -> elevator.distanceTo(call))
        .sortBy { case (_, d) => d }
        .head

      if (distance.canAnswer) { elevator.answer(call); true }
      else { false }
    }

  }

}
