package relayvatr.control

/**
  * Status of a given elevator.
  *
  * @param floor current floor
  * @param direction the travel direction
  */
case class ElevatorStatus(floor: Int, direction: Option[Direction])
