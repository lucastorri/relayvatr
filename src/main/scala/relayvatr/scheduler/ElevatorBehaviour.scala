package relayvatr.scheduler

import relayvatr.control.Direction
import relayvatr.event.{Call, ElevatorEvent}

/**
  * An object that describes how an elevator will handle new user actions.
  */
trait ElevatorBehaviour {

  /**
    * @return the id of the elevator being described
    */
  def id: String

  /**
    * @return the current floor of the elevator
    */
  def floor: Int

  /**
    * `Some` direction that the described elevator is travelling, or `None` if it is idle.
    * @return the elevator direction
    */
  def direction: Option[Direction]

  /**
    * Request the elevator to handle a call.
    * @param call the call to be handled
    */
  def answer(call: Call): Unit

  /**
    * Requests the elevator to travel to a specific floor.
    * @param floor the requested floor
    */
  def press(floor: Int): Unit

  /**
    * Calculates a cost associated to this elevator handling a specific call.
    * @param call the call to use as a base for the calculation.
    * @return the cost
    */
  def cost(call: Call): CallCost

  /**
    * Performs the next action that this elevator will do, be it going in any direction or standing still when idle.
    * @return the event describing the elevator action
    */
  def move(): Option[ElevatorEvent]

}
