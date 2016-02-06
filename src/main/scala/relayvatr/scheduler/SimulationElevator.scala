package relayvatr.scheduler


import relayvatr.control._
import relayvatr.event._

import scala.collection.mutable

class SimulationElevator(val id: String) {

  private var currentFloor = 0
  private var currentDirection = Option.empty[Direction]
  private val pressedFloors = mutable.HashSet.empty[Int]
  private var pendingCalls = mutable.ListBuffer.empty[Call]
  private var noStop = false

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
        val destinationFloor = pendingCalls.head.floor
        val newDirection = directionOf(destinationFloor)
        currentDirection = Some(newDirection)
        pendingCalls.remove(0)
        val (same, opposite) = pendingCalls.partition(onSameDirection)
        pendingCalls = opposite
        press(destinationFloor)
        same.foreach(call => press(call.floor))

        ElevatorArrived(id, currentFloor, newDirection)
      case Some(direction) if pressedFloors.contains(currentFloor) =>
        pressedFloors.remove(currentFloor)
        if (pressedFloors.isEmpty) {
          currentDirection = None
          ElevatorArrived(id, currentFloor, direction)
        } else if (pressedFloors.exists(direction.isOnDirection(currentFloor, _))) {
          ElevatorArrived(id, currentFloor, direction)
        } else {
          currentDirection = Some(direction.opposite)
          ElevatorArrived(id, currentFloor, direction.opposite)
        }
      case Some(direction) if pressedFloors.isEmpty =>
        currentDirection = None
        null
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

  def press(floor: Int): Unit = {
    pressedFloors.add(floor)
  }

}
