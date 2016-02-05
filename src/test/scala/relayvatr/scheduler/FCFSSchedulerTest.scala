package relayvatr.scheduler

import relayvatr.exception.InvalidFloorException
import relayvatr.{Test, _}
import relayvatr.control.{BasicControl, ControlConfig, RangeLimitSensor}
import relayvatr.event.ElevatorArrived
import relayvatr.user.SingleTrip

import scala.concurrent.Await
import scala.concurrent.duration._

trait FCFSSchedulerTest extends Test {

  val firstFloor = 0
  val lastFloor = 6
  val config = ControlConfig(1, 10.seconds, new RangeLimitSensor(firstFloor, lastFloor))

  def scheduler: FCFSScheduler
  def control = new BasicControl(scheduler)

  it must "attend user requests" in {

    val trip = SingleTrip(firstFloor, lastFloor)
    val events = control.execute(trip)

    events must equal (Seq(
      ElevatorArrived("elevator-1", trip.startingFloor),
      ElevatorArrived("elevator-1", trip.destinationFloor)
    ))
  }

  it must "check call floor" in {

    val trip = SingleTrip(firstFloor-1, lastFloor)

    an [InvalidFloorException] must be thrownBy {
      Await.result(trip.on(control), 5.seconds)
    }
  }

  it must "check destination floor" in {

    val trip = SingleTrip(firstFloor, lastFloor+1)

    an [InvalidFloorException] must be thrownBy {
      Await.result(trip.on(control), 5.seconds)
    }
  }

}
