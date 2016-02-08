package relayvatr.control

/**
  * An object describing the travel direction of an elevator.
  */
sealed trait Direction {

  /**
    * Natural counting of a given direction. 1 if going up, -1 if going down.
    * @return the count number
    */
  def count: Int

  /**
    * @return The direction opposite to this
    */
  def opposite: Direction

  /**
    * Check if the travel direction between two floors is the same as this.
    *
    * @param currentFloor the starting floor
    * @param destinationFloor the destination floor
    * @return true if the specified travel follows this direction
    */
  def isOnDirection(currentFloor: Int, destinationFloor: Int): Boolean

}

/**
  * `Direction` of an elevator going up.
  */
case object Up extends Direction {
  override val count: Int = +1
  override def opposite: Direction = Down
  override def isOnDirection(currentFloor: Int, destinationFloor: Int): Boolean = currentFloor < destinationFloor
}

/**
  * `Direction` of an elevator going down.
  */
case object Down extends Direction {
  override val count: Int = -1
  override def opposite: Direction = Up
  override def isOnDirection(currentFloor: Int, destinationFloor: Int): Boolean = currentFloor > destinationFloor
}
