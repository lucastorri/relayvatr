package relayvatr.scheduler

import com.typesafe.scalalogging.StrictLogging
import relayvatr.control._
import relayvatr.event._

import scala.collection.mutable

class SameDirectionElevator(
  val id: String,
  initialFloor: Int = 0,
  changeDirectionsAfterNIdleIterations: Int = 3
) extends ElevatorBehaviour with StrictLogging {

  private var currentFloor = initialFloor
  private var currentDirection = Option.empty[Direction]
  private var newDirection = Option.empty[Direction]
  private val pressedFloors = mutable.HashSet.empty[Int]
  private var pendingCalls = mutable.ListBuffer.empty[Call]
  private var idleIterations = 0
  private var nextIdleDirection: Direction = Up

  override def floor: Int = currentFloor

  override def direction: Option[Direction] = currentDirection.orElse(newDirection)

  override def answer(call: Call): Unit = {
    if (newDirection.isEmpty && call.floor != currentFloor) {
      val direction = directionOf(call.floor)
      newDirection = Some(direction)
      logger.debug(s"Going $direction on $id")
    }
    pendingCalls.append(call)
    if (call.floor != currentFloor) addPressed(call.floor)
    logger.debug(s"Pending on $id: ${pendingCalls.mkString(", ")}")
  }

  override def press(floor: Int): Unit = {
    logger.debug(s"Pressed $floor on $id")
    addPressed(floor)
  }

  override def distanceTo(call: Call): CallDistance = {
    if (canAnswer(call)) OnTheWay(math.abs(call.floor - currentFloor) * (pendingCalls.size + pressedFloors.size + 1))
    else CanNotAnswer
  }

  override def move(): Option[ElevatorEvent] = {
    moveAndReport() match {
      case Some(state) =>
        idleIterations = 0
        Some(state)
      case None =>
        idleIterations += 1
        changeIfIdle()
    }
  }

  private def moveAndReport() = Option {
    currentDirection match {
      case None if newDirection.isDefined =>
        currentDirection = newDirection
        newDirection = None
        leaving(currentDirection.get)
      case None if pendingCalls.isEmpty =>
        null
      case None if pendingCalls.head.floor == currentFloor =>
        val direction = nextPending().direction
        currentDirection = Some(direction)
        arrive(direction)
      case None =>
        val destinationFloor = nextPending().floor

        val newDirection = directionOf(destinationFloor)
        currentDirection = Some(newDirection)

        val (same, opposite) = pendingCalls.partition(isOnSameDirection)
        pendingCalls = opposite

        addPressed(destinationFloor)
        same.foreach(call => addPressed(call.floor))

        arrive(newDirection)
      case Some(direction) if pressedFloors.contains(currentFloor) =>
        pressedFloors.remove(currentFloor)
        if (pressedFloors.isEmpty) {
          currentDirection = None
          arrive(direction)
        } else if (areThereCallsOn(direction)) {
          arrive(direction)
        } else {
          null
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

  private def changeIfIdle(): Option[ElevatorArrived] = {
    if (idleIterations % changeDirectionsAfterNIdleIterations == 0) {
      val state = arrive(nextIdleDirection)
      nextIdleDirection = nextIdleDirection.opposite
      Some(state)
    } else {
      None
    }
  }

  private def nextPending(): Call = {
    pendingCalls.remove(0)
  }

  private def leaving(nextDirection: Direction): ElevatorLeaving = {
    ElevatorLeaving(id, currentFloor, nextDirection)
  }

  private def arrive(nextDirection: Direction): ElevatorArrived = {
    pendingCalls = pendingCalls.filterNot(call => call.floor == currentFloor && call.direction == nextDirection)
    ElevatorArrived(id, currentFloor, nextDirection)
  }

  private def addPressed(floor: Int): Unit = {
    if (currentDirection.isEmpty && newDirection.isEmpty) {
      newDirection = Some(directionOf(floor))
    }
    pressedFloors.add(floor)
  }

  private def areThereCallsOn(direction: Direction): Boolean = {
    pressedFloors.exists(direction.isOnDirection(currentFloor, _))
  }

  private def canAnswer: (Call) => Boolean = {
    case _ if currentDirection.isEmpty && newDirection.isEmpty && pendingCalls.isEmpty => true
    case call if newDirection.contains(directionOf(call.floor)) && call.direction == directionOf(call.floor) => true
    case call if currentDirection.contains(directionOf(call.floor)) && call.direction == directionOf(call.floor) => true
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
