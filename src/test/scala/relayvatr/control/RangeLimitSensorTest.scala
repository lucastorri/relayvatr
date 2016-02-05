package relayvatr.control

import relayvatr.Test

class RangeLimitSensorTest extends Test {

  it must "check if floor is out of bounds" in {
    val sensor = new RangeLimitSensor(0, 5)

    sensor.canGoTo(0) must be (true)
    sensor.canGoTo(3) must be (true)
    sensor.canGoTo(5) must be (true)

    sensor.canGoTo(-1) must be (false)
    sensor.canGoTo(6) must be (false)
  }

}
