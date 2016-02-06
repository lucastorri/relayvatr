package relayvatr.control

import scala.concurrent.Future

trait Elevator {
  def id: String
  def goTo(floor: Int): Future[Unit]
}
