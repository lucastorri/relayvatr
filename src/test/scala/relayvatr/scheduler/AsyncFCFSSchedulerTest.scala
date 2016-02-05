package relayvatr.scheduler

import relayvatr._
import relayvatr.control.{BasicControlSystem, ControlSystemConfig, RangeLimitSensor}
import relayvatr.event.ElevatorArrived
import relayvatr.user.SingleTrip

import scala.concurrent.duration._

class AsyncFCFSSchedulerTest extends Test {

  val config = ControlSystemConfig(1, 10.seconds, new RangeLimitSensor(0, 6))

  it must "attend user requests" in {

    val trip = SingleTrip(0, 5)
    val events = new BasicControlSystem(new AsyncFCFSScheduler(config)).execute(trip)

    events must equal (Seq(
      ElevatorArrived("elevator-1", trip.startingFloor),
      ElevatorArrived("elevator-1", trip.destinationFloor)
    ))

  }

}
