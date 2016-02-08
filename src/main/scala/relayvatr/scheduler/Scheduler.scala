package relayvatr.scheduler

import relayvatr.control.ElevatorStatus
import relayvatr.event._
import rx.lang.scala.Observable

/**
  * An object that receives actions performed by users and decides how elevators will respond to them.
  */
trait Scheduler {

  /**
    * Describes the current state of each elevator controlled by this scheduler.
    * @return elevators status
    */
  def status: Set[ElevatorStatus]

  /**
    * Events describe elevator reactions taken by the scheduler after use actions.
    * @return a stream of elevator events
    */
  def events: Observable[ElevatorEvent]

  /**
    * Receives all the actions that users made on the system in order to decide future movements.
    * @param action the action performed by an user
    */
  def handle(action: Action): Unit

  /**
    * Terminates this scheduler.
    */
  def shutdown(): Unit

}



