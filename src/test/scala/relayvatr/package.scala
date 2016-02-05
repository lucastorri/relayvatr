import relayvatr.control.ControlSystem
import relayvatr.event.SystemEvent
import relayvatr.user.UserTrip

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

package object relayvatr {

  implicit class EventsCollector(system: ControlSystem)(implicit exec: ExecutionContext) {

    private val collected = mutable.ListBuffer.empty[SystemEvent]
    system.events.subscribe(collected += _)

    def execute(trip: UserTrip): Seq[SystemEvent] = {
      Await.ready(trip.on(system), 5.seconds)
      collected.toSeq
    }

  }

}
