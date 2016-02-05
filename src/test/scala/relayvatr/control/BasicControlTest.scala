package relayvatr.control

import relayvatr._
import relayvatr.event.{Action, ActionEvent, SystemEvent, SystemShutdown}
import relayvatr.scheduler.Scheduler
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

class BasicControlTest extends Test {

  it must "generate a shutdown event" in {
    val events = ReplaySubject[SystemEvent]()
    val system = new BasicControl(NoOpScheduler)

    system.events.subscribe(events)
    system.shutdown()

    events.toBlocking.toIterable.toSeq must equal (Seq(
      SystemShutdown
    ))
  }

  object NoOpScheduler extends Scheduler {
    override def events: Observable[ActionEvent] = Observable.just()
    override def shutdown(): Unit = {}
    override def handle(action: Action): Unit = {}
  }

}
