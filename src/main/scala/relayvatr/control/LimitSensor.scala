package relayvatr.control

trait LimitSensor {

  def canGoTo(floor: Int): Boolean

}

class RangeLimitSensor(firstFloor: Int, lastFloor: Int) extends LimitSensor {
  override def canGoTo(floor: Int): Boolean = floor >= firstFloor && floor <= lastFloor
}