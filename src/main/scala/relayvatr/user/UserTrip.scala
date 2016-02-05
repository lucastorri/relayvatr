package relayvatr.user

import relayvatr.control.{Control, Direction, Down, Up}

import scala.concurrent.{ExecutionContext, Future}

trait UserTrip {
  def on(control: Control): Future[Unit]
}

case class SingleTrip(startingFloor: Int, destinationFloor: Int)(implicit exec: ExecutionContext) extends UserTrip {

  override def on(control: Control): Future[Unit] = {
    for {
      elevator <- control.call(startingFloor, direction)
      arrival <- elevator.goTo(destinationFloor)
    } yield ()
  }

  def direction: Direction = if (isGoingUp) Up else Down

  private val isGoingUp = startingFloor < destinationFloor

}

case class CombinedTrips(trips: Set[SingleTrip])(implicit exec: ExecutionContext) extends UserTrip {

  override def on(control: Control): Future[Unit] = {
    Future.sequence(trips.map(_.on(control))).map(_ => ())
  }

}

object CombinedTrips {

  def apply(trips: SingleTrip*)(implicit exec: ExecutionContext): CombinedTrips = apply(trips.toSet)

}