package relayvatr.control

import scala.concurrent.duration.Duration

case class ControlSystemConfig(elevators: Int, travelTimePerFloor: Duration, limit: LimitSensor)
