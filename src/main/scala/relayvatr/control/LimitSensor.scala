package relayvatr.control

/**
  * An object that check if an elevator can reach a given floor.
  */
trait LimitSensor {

  /**
    * Check if the floor is reachable.
    *
    * @param floor the floor that the elevator wants to know if is valid
    * @return true if the floor is valid
    */
  def canGoTo(floor: Int): Boolean

}

/**
  * A `LimitSensor` that limits elevators to a given range.
  *
  * @param firstFloor the lower floor
  * @param lastFloor the highest floor
  */
class RangeLimitSensor(firstFloor: Int, lastFloor: Int) extends LimitSensor {

  override def canGoTo(floor: Int): Boolean = floor >= firstFloor && floor <= lastFloor

}