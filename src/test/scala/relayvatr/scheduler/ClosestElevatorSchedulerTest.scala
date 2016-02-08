package relayvatr.scheduler

import relayvatr._
import relayvatr.control.{BasicControl, ControlConfig, RangeLimitSensor}
import relayvatr.event.{ElevatorArrived, ElevatorEvent}
import relayvatr.user.{CombinedTrips, SingleTrip}
import rx.lang.scala.Observable

import scala.concurrent.duration._

class ClosestElevatorSchedulerTest extends Test {

  val clock = Observable.interval(10.millis).map(_ => ())
  val config = ControlConfig(2, new RangeLimitSensor(0, 5))
  val control = new BasicControl(new ClosestElevatorScheduler(config, clock, new SameDirectionElevator(_)))

  val trips = CombinedTrips(
    SingleTrip(5, 0),
    SingleTrip(0, 3),
    SingleTrip(1, 4),
    SingleTrip(2, 5))

  val events = control.execute(trips)

  it must "reach floors with user calls" in {

    val reachedFloors = events.collect { case ea: ElevatorArrived => ea.floor }.toSet
    val calledFloors = trips.each.map(_.startingFloor)

    calledFloors.forall(reachedFloors.contains) must be (true)
  }

  it must "go to floors users selected" in {

    val reachedFloors = events.collect { case ea: ElevatorArrived => ea.floor }.toSet
    val destinationFloors = trips.each.map(_.destinationFloor)

    destinationFloors.forall(reachedFloors.contains) must be (true)
  }

  it must "move smoothly between floors" in {

    val elevatorTrips = events
      .collect { case e: ElevatorEvent => e }
      .groupBy(_.elevatorId)

    elevatorTrips must not be empty

    elevatorTrips.values.foreach { trip =>
      trip.zip(trip.tail).forall(isMovingSequentially.tupled) must be (true)
    }
  }

  private val isMovingSequentially = (e0: ElevatorEvent, e1: ElevatorEvent) => {
    math.abs(e0.floor - e1.floor) <= 1
  }

}
