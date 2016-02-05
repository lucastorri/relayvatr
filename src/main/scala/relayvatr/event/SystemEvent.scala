package relayvatr.event

sealed trait SystemEvent

trait ElevatorEvent extends SystemEvent {
  def elevatorId: String
}

case class ElevatorArrived(elevatorId: String, floor: Int) extends ElevatorEvent

case object SystemShutdown extends SystemEvent
