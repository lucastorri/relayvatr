package relayvatr.exception

/**
  * Error thrown if a scheduler cannot attend a given call or go to a specific floor.
  */
case class CannotHandleCallException() extends Exception(s"Cannot handle this call")
