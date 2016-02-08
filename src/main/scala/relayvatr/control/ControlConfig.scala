package relayvatr.control

/**
  * Specific configurations to the system.
  *
  * @param elevators the number of available elevators
  * @param limit describe the limits of this system
  */
case class ControlConfig(elevators: Int, limit: LimitSensor)
