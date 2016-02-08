package relayvatr.scheduler

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.scalalogging.StrictLogging
import relayvatr.control._
import relayvatr.event._
import relayvatr.exception.InvalidFloorException
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable

class ClosestElevatorScheduler(
  config: ControlConfig,
  clock: Observable[Unit],
  elevatorFactory: (String) => ElevatorBehaviour
)(implicit system: ActorSystem) extends Scheduler with StrictLogging {

  private val handler = system.actorOf(Props(new Handler))
  private val eventsSubject = Subject[ElevatorEvent]()
  private var awaitingCalls = mutable.ListBuffer.empty[Call]
  private val elevators = (1 to config.elevators).map(newElevator).toMap
  private val clockSubscription = clock.subscribe(_ => handler ! 'clock)

  override def status: Set[ElevatorStatus] =
    elevators.values.map(e => ElevatorStatus(e.floor, e.direction)).toSet

  override def events: Observable[ElevatorEvent] = eventsSubject

  override def handle(action: Action): Unit = {
    logger.debug(s"Action $action")
    action match {
      case call: Call if isInvalidFloor(call.floor) => throw new InvalidFloorException(call.floor)
      case goto: GoTo if isInvalidFloor(goto.floor) => throw new InvalidFloorException(goto.floor)
      case _ => handler ! action
    }
  }

  override def shutdown(): Unit = {
    clockSubscription.unsubscribe()
    system.stop(handler)
    eventsSubject.onCompleted()
  }

  private def isInvalidFloor(floor: Int): Boolean =
    !config.limit.canGoTo(floor)

  private def newElevator(i: Int): (String, ElevatorBehaviour) = {
    val id = s"elevator-$i"
    id -> elevatorFactory(id)
  }

  private class Handler extends Actor {

    override def receive: Receive = {
      case a: Action =>
        act(a)
      case 'clock =>
        awaitingCalls = awaitingCalls.filterNot(answeredToCall)
        elevators.values.flatMap(_.move()).foreach(eventsSubject.onNext)
    }

    val act: PartialFunction[Action, Unit] = {
      case call: Call =>
        awaitingCalls += call
      case goto: GoTo =>
        elevators.get(goto.elevatorId).foreach(_.press(goto.floor))
    }

    def answeredToCall(call: Call): Boolean = {
      val (elevator, lowestCost) = elevators.values.toSeq
        .map(elevator => elevator -> elevator.distanceTo(call))
        .sortBy { case (_, cost) => cost }
        .head

      if (lowestCost.canAnswer) {
        logger.debug(s"Assigning $call to ${elevator.id}")
        elevator.answer(call)
      }

      lowestCost.canAnswer
    }

  }

}
