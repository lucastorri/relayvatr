package relayvatr.event

import relayvatr.control.Direction

/**
  * An object that describes events that happened throughout the system.
  */
sealed trait SystemEvent

/**
  * A specific `SystemEvent` emitted by the elevators.
  */
trait ElevatorEvent extends SystemEvent {

  /**
    * @return the elevator associated to the event
    */
  def elevatorId: String

  /**
    *
    * @return the floor where the event happened
    */
  def floor: Int

  /**
    * @return the direction that the elevator is travelling
    */
  def direction: Direction
}

/**
  * A `ElevatorEvent` announcing that a given elevator arrived at a floor.
  */
case class ElevatorArrived(elevatorId: String, floor: Int, direction: Direction) extends ElevatorEvent

/**
  * A `ElevatorEvent` announcing that a given elevator is leaving the floor that it recently arrived.
  */
case class ElevatorLeaving(elevatorId: String, floor: Int, direction: Direction) extends ElevatorEvent

/**
  * A `ElevatorEvent` announcing that a given elevator is passing without stopping at the floor.
  */
case class ElevatorPassing(elevatorId: String, floor: Int, direction: Direction) extends ElevatorEvent

/**
  * A `SystemEvent` announcing that the system is being terminated.
  */
case object SystemShutdown extends SystemEvent
