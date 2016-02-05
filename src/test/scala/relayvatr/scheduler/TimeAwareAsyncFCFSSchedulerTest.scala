package relayvatr.scheduler

import relayvatr._
import relayvatr.control.{BasicControl, ControlConfig, RangeLimitSensor}
import relayvatr.event.{TimestampedEvent, ElevatorArrived}
import relayvatr.user.{CombinedTrips, SingleTrip}

import scala.concurrent.duration._

class TimeAwareAsyncFCFSSchedulerTest extends Test {

  val config = ControlConfig(2, 600.millis, new RangeLimitSensor(0, 5))

  it must "travel multiple times" in {
    val travelTime = 3 * config.travelTimePerFloor
    val trip = CombinedTrips(
      SingleTrip(0, 3),
      SingleTrip(1, 4),
      SingleTrip(2, 5))

    val control = new BasicControl(new TimeAwareAsyncFCFSScheduler(config))

    val pairs = control.executeWithTimestamps(trip)

    val visitedFloors = withArrivals(pairs, (_, ea) => ea.floor)
    val timestamps = withArrivals(pairs, (t, _) => t)

    visitedFloors must equal (Seq(0, 1, 3, 4, 2, 5))

    timestamps(2) - timestamps(0) must be > travelTime.toMillis
    timestamps(3) - timestamps(1) must be > travelTime.toMillis
    timestamps(5) - timestamps(4) must be > travelTime.toMillis
  }

  def withArrivals[T](pairs: Seq[TimestampedEvent], f: (Long, ElevatorArrived) => T): Seq[T] =
    pairs.collect { case TimestampedEvent(ea: ElevatorArrived, t) => f(t, ea) }

}
