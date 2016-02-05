package relayvatr.event

sealed trait SystemEvent

trait ElevatorEvent extends SystemEvent

case class ElevatorArrived(elevatorId: String, floor: Int) extends ElevatorEvent

case class InvalidFloor(floor: Int) extends ElevatorEvent

case object SystemShutdown extends SystemEvent
