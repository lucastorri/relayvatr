package relayvatr.control

import relayvatr._
import relayvatr.event.{Action, ActionEvent, SystemEvent, SystemShutdown}
import relayvatr.scheduler.SystemScheduler
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

class BasicControlSystemTest extends Test {

  it must "generate a shutdown event" in {
    val events = ReplaySubject[SystemEvent]()
    val system = new BasicControlSystem(NoOpScheduler)

    system.events.subscribe(events)
    system.shutdown()

    events.toBlocking.toIterable.toSeq must equal (Seq(
      SystemShutdown
    ))
  }

  object NoOpScheduler extends SystemScheduler {
    override def events: Observable[ActionEvent] = Observable.just()
    override def shutdown(): Unit = {}
    override def handle(action: Action): Unit = {}
  }

}
