package relayvatr.scheduler

import relayvatr.event.{Action, ActionEvent}
import rx.lang.scala.Observable

trait SystemScheduler {

  def events: Observable[ActionEvent]

  def handle(action: Action): Unit

  def shutdown(): Unit

}
