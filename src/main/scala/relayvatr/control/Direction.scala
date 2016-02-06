package relayvatr.control

sealed trait Direction {
  def count: Int
  def opposite: Direction
  def isOnDirection(currentFloor: Int, destinationFloor: Int): Boolean
}

case object Up extends Direction {
  override val count: Int = +1
  override def opposite: Direction = Down
  override def isOnDirection(currentFloor: Int, destinationFloor: Int): Boolean = currentFloor < destinationFloor
}
case object Down extends Direction {
  override val count: Int = -1
  override def opposite: Direction = Up
  override def isOnDirection(currentFloor: Int, destinationFloor: Int): Boolean = currentFloor > destinationFloor
}
