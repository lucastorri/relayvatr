package relayvatr.user

import relayvatr.control.{ControlSystem, Direction, Down, Up}

import scala.concurrent.{ExecutionContext, Future}

trait UserTrip {
  def on(system: ControlSystem): Future[Unit]
}

case class SingleTrip(startingFloor: Int, destinationFloor: Int)(implicit exec: ExecutionContext) extends UserTrip {

  override def on(system: ControlSystem): Future[Unit] = {
    for {
      elevator <- system.call(startingFloor, direction)
      arrival <- elevator.goTo(destinationFloor)
    } yield ()
  }

  def direction: Direction = if (isGoingUp) Up else Down

  private val isGoingUp = startingFloor < destinationFloor

}