package relayvatr.scheduler

import relayvatr.event.ActionEvent
import rx.lang.scala.{Observable, Subject}

trait EventSubject { self: SystemScheduler =>

  protected val subject = Subject[ActionEvent]()

  override def events: Observable[ActionEvent] = subject

}
