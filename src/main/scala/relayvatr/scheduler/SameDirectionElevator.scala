package relayvatr.scheduler


import com.typesafe.scalalogging.StrictLogging
import relayvatr.control._
import relayvatr.event._

import scala.collection.mutable


class SameDirectionElevator(val id: String) extends ElevatorBehaviour with StrictLogging {

  private var currentFloor = 0
  private var currentDirection = Option.empty[Direction]
  private val pressedFloors = mutable.HashSet.empty[Int]
  private var pendingCalls = mutable.ListBuffer.empty[Call]

  override def floor: Int = currentFloor

  override def answer(call: Call): Unit = {
    pendingCalls.append(call)
    logger.debug(s"Pending on $id: ${pendingCalls.mkString(", ")}")
  }

  override def press(floor: Int): Unit = {
    logger.debug(s"Pressed $floor on $id")
    addPressed(floor)
  }

  override def distanceTo(call: Call): CallDistance = {
    if (canAnswer(call)) OnTheWay(math.abs(call.floor - currentFloor))
    else CanNotAnswer
  }

  override def move(): Option[ElevatorEvent] = Option {
    currentDirection match {
      case None if pendingCalls.isEmpty =>
        null
      case None if pendingCalls.head.floor == currentFloor =>
        val direction = pendingCalls.remove(0).direction
        currentDirection = Some(direction)
        ElevatorArrived(id, currentFloor, direction)
      case None =>
        val destinationFloor = pendingCalls.head.floor
        pendingCalls.remove(0)

        val newDirection = directionOf(destinationFloor)
        currentDirection = Some(newDirection)

        val (same, opposite) = pendingCalls.partition(isOnSameDirection)
        pendingCalls = opposite

        addPressed(destinationFloor)
        same.foreach(call => addPressed(call.floor))

        if (destinationFloor == currentFloor) ElevatorArrived(id, currentFloor, newDirection)
        else ElevatorLeaving(id, currentFloor, newDirection)
      case Some(direction) if pressedFloors.contains(currentFloor) =>
        pressedFloors.remove(currentFloor)
        if (pressedFloors.isEmpty) {
          currentDirection = None
          ElevatorArrived(id, currentFloor, direction)
        } else if (areThereCallsOn(direction)) {
          ElevatorArrived(id, currentFloor, direction)
        } else {
          currentDirection = Some(direction.opposite)
          ElevatorArrived(id, currentFloor, direction.opposite)
        }
      case Some(direction) if pressedFloors.isEmpty =>
        currentDirection = None
        null
      case Some(direction) =>
        val previousFloor = currentFloor
        currentFloor += direction.count
        ElevatorPassing(id, previousFloor, direction)
    }
  }

  private def addPressed(floor: Int): Unit = {
    pressedFloors.add(floor)
  }

  private def areThereCallsOn(direction: Direction): Boolean = {
    pressedFloors.exists(direction.isOnDirection(currentFloor, _))
  }

  private def canAnswer: (Call) => Boolean = {
    case _ if currentDirection.isEmpty && pendingCalls.isEmpty => true
    case Call(floor, _) if currentDirection.isEmpty && directionOf(floor) == pendingCalls.head.direction => true
    case Call(floor, _) if currentDirection.exists(_.isOnDirection(currentFloor, floor)) => true
    case _ => false
  }

  private def directionOf(floor: Int): Direction = {
    if (floor > currentFloor) Up else Down
  }

  private def isOnSameDirection(call: Call): Boolean = {
    currentDirection match {
      case Some(direction) => call.direction == direction && direction.isOnDirection(currentFloor, call.floor)
      case None => false
    }
  }

}
