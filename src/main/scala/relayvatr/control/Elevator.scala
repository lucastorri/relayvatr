package relayvatr.control

import scala.concurrent.Future

/**
  * An interface that a user, after entering the elevator, requests the floor he/she wants to reach.
  */
trait Elevator {

  /**
    * @return the id of this elevator
    */
  def id: String

  /**
    * Requests the elevator to move to the specified floor
    * @param floor destination floor
    * @return a future that will be completed when the floor is reached
    */
  def goTo(floor: Int): Future[Unit]

}
