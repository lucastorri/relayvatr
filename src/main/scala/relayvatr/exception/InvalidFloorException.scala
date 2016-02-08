package relayvatr.exception

/**
  * Describes an error that occurred when a user tried to reach an invalid floor.
  *
  * @param floor the offending floor number
  */
case class InvalidFloorException(floor: Int) extends Exception(s"Floor $floor doesn't exists")
