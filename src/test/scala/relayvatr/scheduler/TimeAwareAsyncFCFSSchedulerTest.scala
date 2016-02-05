package relayvatr.scheduler

import relayvatr._
import relayvatr.control.{BasicControl, RangeLimitSensor, ControlConfig}
import relayvatr.event.ElevatorArrived
import relayvatr.user.{SingleTrip, CombinedTrips}
import scala.concurrent.duration._

class TimeAwareAsyncFCFSSchedulerTest extends Test {

  val config = ControlConfig(2, 600.millis, new RangeLimitSensor(0, 5))

  it must "travel multiple times" in {
    val trip = CombinedTrips(
      SingleTrip(0, 3),
      SingleTrip(1, 4),
      SingleTrip(2, 5))

    val control = new BasicControl(new TimeAwareAsyncFCFSScheduler(config))

    val visitedFloors = control.execute(trip).collect { case ElevatorArrived(_, floor) => floor }

    visitedFloors must equal (Seq(
      0, 1, 3, 4, 2, 5
    ))
  }

}
