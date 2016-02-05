package relayvatr.control

import scala.concurrent.duration.FiniteDuration

case class ControlConfig(elevators: Int, travelTimePerFloor: FiniteDuration, limit: LimitSensor)
