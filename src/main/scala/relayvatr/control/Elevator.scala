package relayvatr.control

import scala.concurrent.Future

trait Elevator {
  def id: String
  def goTo(n: Int): Future[Unit]
}
