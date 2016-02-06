package relayvatr.scheduler

import akka.actor.{Actor, ActorSystem, Props}
import relayvatr.control._
import relayvatr.event._
import relayvatr.exception.InvalidFloorException
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable

trait Scheduler {

  def events: Observable[ElevatorEvent]

  def handle(action: Action): Unit

  def shutdown(): Unit

}

class NewScheduler(config: ControlConfig)(implicit system: ActorSystem) extends Scheduler {

  private val handler = system.actorOf(Props(new Handler))
  private val subject = Subject[ElevatorEvent]()
  private var awaitingCalls = mutable.ListBuffer.empty[Call]
  private val elevators = (1 to config.elevators).map(i => new Elevator(s"elevator-$i"))

  override def events: Observable[ElevatorEvent] = subject

  override def handle(action: Action): Unit = action match {
    case call: Call if isInvalidFloor(call.floor) => throw new InvalidFloorException(call.floor)
    case goto: GoTo if isInvalidFloor(goto.floor) => throw new InvalidFloorException(goto.floor)
    case _ => handler ! action
  }

  private def isInvalidFloor(floor: Int): Boolean =
    !config.limit.canGoTo(floor)

  override def shutdown(): Unit = {
    system.stop(handler)
    subject.onCompleted()
  }

  private class Handler extends Actor {

    var currentCycle = 0L

    override def receive: Receive = {
      case a: Action =>
        act(a)
      case 'clock =>
        awaitingCalls = awaitingCalls.filterNot(canAnswer)
        elevators.flatMap(_.move()).foreach(emit)
    }

    val act: PartialFunction[Action, Unit] = {
      case call: Call =>
        awaitingCalls += call
      case goto: GoTo =>
        elevators.find(_.id == goto.elevatorId).foreach(_.press(goto.floor))
    }

    def canAnswer(call: Call): Boolean = {
      val (elevator, distance) = elevators
        .map(elevator => elevator -> elevator.distanceOf(call))
        .sortBy { case (_, d) => d }
        .head

      if (distance.canAnswer) { elevator.answer(call); true }
      else { false }
    }

  }

  private def emit(event: ElevatorEvent): Unit = {
    subject.onNext(event)
  }

}

class Elevator(val id: String) {

  private var currentFloor = 0
  private var currentDirection = Option.empty[Direction]
  private val pressedFloors = mutable.HashSet.empty[Int]
  private var pendingCalls = mutable.ListBuffer.empty[Call]

  def answer(call: Call): Unit = {
    pendingCalls += call
  }

  def distanceOf(call: Call): CallDistance = {
    if (canAnswer.isDefinedAt(call)) OnTheWay(math.abs(call.floor - currentFloor))
    else CanNotAnswer
  }

  private def canAnswer: PartialFunction[Call, Unit] = {
    case _ if currentDirection.isEmpty && pendingCalls.isEmpty =>
    case Call(floor, _) if currentDirection.isEmpty && directionOf(floor) == pendingCalls.head.direction =>
    case Call(floor, _) if currentDirection.exists(_.isOnDirection(currentFloor, floor)) =>
  }

  private def directionOf(floor: Int): Direction = {
    if (floor > currentFloor) Up else Down
  }

  def move(): Option[ElevatorEvent] = Option {
    currentDirection match {
      case None if pendingCalls.isEmpty =>
        null
      case None =>
        val newDirection = pendingCalls.head.direction
        currentDirection = Some(newDirection)
        pendingCalls.remove(0)
        val (same, opposite) = pendingCalls.partition(onSameDirection)
        pendingCalls = opposite
        same.foreach(call => press(call.floor))

        ElevatorArrived(id, currentFloor, newDirection)
      case Some(direction) if pressedFloors.contains(currentFloor) =>
        pressedFloors.remove(currentFloor)
        if (pressedFloors.isEmpty) {
          currentDirection = None
          null
        } else if (pressedFloors.exists(direction.isOnDirection(currentFloor, _))) {
          ElevatorArrived(id, currentFloor, direction)
        } else {
          currentDirection = Some(direction.opposite)
          ElevatorArrived(id, currentFloor, direction.opposite)
        }
      case Some(direction) =>
        currentFloor += direction.count
        ElevatorPassing(id, currentFloor, direction)
    }
  }

  private def onSameDirection(call: Call): Boolean = {
    currentDirection match {
      case Some(direction) => call.direction == direction && direction.isOnDirection(currentFloor, call.floor)
      case None => false
    }
  }

  def press(floor: Int): Unit =
    pressedFloors.add(floor)

}