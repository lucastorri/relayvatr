package relayvatr.control

import relayvatr._
import relayvatr.event._
import relayvatr.scheduler.Scheduler
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

class BasicControlTest extends Test {

  it must "generate a shutdown event" in {
    val events = ReplaySubject[SystemEvent]()
    val control = new BasicControl(NoOpScheduler)

    control.events.subscribe(events)
    control.shutdown()

    events.toBlocking.toIterable.toSeq must equal (Seq(
      SystemShutdown
    ))
    control.status.running must be (false)
  }

  it must "return it's status" in {
    val control = new BasicControl(NoOpScheduler)

    control.status must equal(ControlStatus(running = true, NoOpScheduler.status))
  }

  object NoOpScheduler extends Scheduler {
    override def status: Set[ElevatorStatus] = Set(ElevatorStatus(3, None))
    override def events: Observable[ElevatorEvent] = Observable.just()
    override def shutdown(): Unit = {}
    override def handle(action: Action): Unit = {}
  }

}
