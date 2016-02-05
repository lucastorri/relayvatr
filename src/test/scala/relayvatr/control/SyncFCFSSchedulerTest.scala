package relayvatr.control

import relayvatr._
import relayvatr.event.ElevatorArrived
import relayvatr.scheduler.SyncFCFSScheduler
import relayvatr.user.SingleTrip

import scala.concurrent.duration._

class SyncFCFSSchedulerTest extends Test {

  val config = ControlSystemConfig(1, 10.seconds, new RangeLimitSensor(0, 6))

  it must "attend user requests" in {

    val trip = SingleTrip(0, 5)
    val events = new BasicControlSystem(new SyncFCFSScheduler(config)).execute(trip)

    events must equal (Seq(
      ElevatorArrived("elevator-1", trip.startingFloor),
      ElevatorArrived("elevator-1", trip.destinationFloor)
    ))

  }

}
