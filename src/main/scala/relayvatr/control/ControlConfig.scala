package relayvatr.control

import scala.concurrent.duration.Duration

case class ControlConfig(elevators: Int, travelTimePerFloor: Duration, limit: LimitSensor)
