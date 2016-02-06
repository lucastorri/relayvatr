package relayvatr.scheduler

import relayvatr.event._
import rx.lang.scala.Observable

trait Scheduler {

  def events: Observable[ElevatorEvent]

  def handle(action: Action): Unit

  def shutdown(): Unit

}



