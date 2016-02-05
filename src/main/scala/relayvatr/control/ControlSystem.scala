package relayvatr.control

import relayvatr.event.SystemEvent
import rx.lang.scala.Observable

import scala.concurrent.Future

trait ControlSystem {

  def call(currentFloor: Int, direction: Direction): Future[Elevator]

  def events: Observable[SystemEvent]

  def shutdown(): Future[Unit]

}





