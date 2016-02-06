package relayvatr.event

import relayvatr.control.Direction

sealed trait SystemEvent

trait ElevatorEvent extends SystemEvent {
  def elevatorId: String
  def floor: Int
  def direction: Direction
}

case class ElevatorArrived(elevatorId: String, floor: Int, direction: Direction) extends ElevatorEvent
case class ElevatorLeaving(elevatorId: String, floor: Int, direction: Direction) extends ElevatorEvent
case class ElevatorPassing(elevatorId: String, floor: Int, direction: Direction) extends ElevatorEvent

case object SystemShutdown extends SystemEvent
