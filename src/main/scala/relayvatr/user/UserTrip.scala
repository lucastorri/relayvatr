package relayvatr.user

import com.typesafe.scalalogging.StrictLogging
import relayvatr.control.{Control, Direction, Down, Up}

import scala.concurrent.{ExecutionContext, Future}

trait UserTrip {
  def on(control: Control): Future[Unit]
}

case class SingleTrip(
  startingFloor: Int,
  destinationFloor: Int
)(implicit exec: ExecutionContext) extends UserTrip with StrictLogging {

  val name = NameGenerator.newName()

  override def on(control: Control): Future[Unit] = {
    logger.info(s"User $name will travel from $startingFloor to $destinationFloor")
    for {
      elevator <- control.call(startingFloor, direction)
      _ = logger.info(s"User $name entering ${elevator.id} at $startingFloor")
      arrival <- elevator.goTo(destinationFloor)
      _ = logger.info(s"User $name leaving ${elevator.id} at $destinationFloor")
    } yield ()
  }

  def direction: Direction = if (isGoingUp) Up else Down

  private val isGoingUp = startingFloor < destinationFloor

}

case class CombinedTrips(each: Set[SingleTrip])(implicit exec: ExecutionContext) extends UserTrip {

  override def on(control: Control): Future[Unit] = {
    Future.sequence(each.map(_.on(control))).map(_ => ())
  }

}

object CombinedTrips {

  def apply(each: SingleTrip*)(implicit exec: ExecutionContext): CombinedTrips = apply(each.toSet)

}