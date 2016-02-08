package relayvatr.user

import com.typesafe.scalalogging.StrictLogging
import relayvatr.control.{Control, Direction, Down, Up}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Simulates a user traveling on the elevators.
  */
trait UserTrip {

  /**
    * Perform the trip on the given control.
    * @param control the control to be used
    * @return a future that will be ready when the user trip is finished
    */
  def on(control: Control): Future[Unit]

}

/**
  * A `UserTrip` describing a single user that will request an elevator in a given floor and travel to another one.
  * For that, a user must first call an elevator, and when an elevator is available, he/she will dial the final floor.
  *
  * @param startingFloor the floor that the user currently is
  * @param destinationFloor the floor that the user wants to go to
  * @param exec the `ExecutionContext` that will handle responses
  */
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

/**
  * A `UserTrip` that combines multiple trips to be executed simultaneously.
  * @param each all the trips to be executed
  * @param exec `ExecutionContext` used to combine all resulting futures
  */
case class CombinedTrips(each: Set[SingleTrip])(implicit exec: ExecutionContext) extends UserTrip {

  override def on(control: Control): Future[Unit] = {
    Future.sequence(each.map(_.on(control))).map(_ => ())
  }

}

object CombinedTrips {

  def apply(each: SingleTrip*)(implicit exec: ExecutionContext): CombinedTrips = apply(each.toSet)

}