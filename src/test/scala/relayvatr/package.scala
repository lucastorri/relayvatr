import relayvatr.control.Control
import relayvatr.event.{TimestampedEvent, SystemEvent}
import relayvatr.user.UserTrip

import scala.collection.mutable
import scala.compat.Platform
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

package object relayvatr {

  implicit class EventsCollector(system: Control)(implicit exec: ExecutionContext) {

    private val collected = mutable.ListBuffer.empty[TimestampedEvent]
    system.events.subscribe(collected += TimestampedEvent(_, Platform.currentTime))

    def execute(trip: UserTrip): Seq[SystemEvent] =
      executeWithTimestamps(trip).map(_.event)

    def executeWithTimestamps(trip: UserTrip): Seq[TimestampedEvent] = {
      Await.ready(trip.on(system), 5.seconds)
      collected.toSeq
    }

  }

}
