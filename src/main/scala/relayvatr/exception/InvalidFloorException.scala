package relayvatr.exception

case class InvalidFloorException(floor: Int) extends Exception(s"Floor $floor doesn't exists")
