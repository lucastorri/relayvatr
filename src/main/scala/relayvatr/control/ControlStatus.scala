package relayvatr.control

/**
  * An object describing the status of a given `Control` object.
  * @param running is the system running
  * @param elevators status of individual elevators
  */
case class ControlStatus(running: Boolean, elevators: Set[ElevatorStatus])


