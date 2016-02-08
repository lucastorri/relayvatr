package relayvatr.control

import relayvatr.event.SystemEvent
import rx.lang.scala.Observable

import scala.concurrent.Future

/**
  * An object that functions as the user interface for the elevators. It organizes the users queueing in the floors
  * and that are riding the elevators.
  */
trait Control {

  /**
    * Request an elevator and notifies when a suitable one arrived.
    * @param currentFloor the floor where the elevator is needed
    * @param direction the intended direction
    * @return a future returning the elevator that arrived to attend the user
    */
  def call(currentFloor: Int, direction: Direction): Future[Elevator]

  /**
    * @return a stream of events that are happening in the whole system
    */
  def events: Observable[SystemEvent]

  /**
    * @return current system's status
    */
  def status: ControlStatus

  /**
    * Terminates this control.
    * @return a future to be completed when the system is terminated
    */
  def shutdown(): Future[Unit]

}





