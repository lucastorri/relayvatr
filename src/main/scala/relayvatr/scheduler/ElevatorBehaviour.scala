package relayvatr.scheduler

import relayvatr.control.Direction
import relayvatr.event.{Call, ElevatorEvent}

trait ElevatorBehaviour {

  def id: String

  def floor: Int

  def direction: Option[Direction]

  def answer(call: Call): Unit

  def press(floor: Int): Unit

  def distanceTo(call: Call): CallCost

  def move(): Option[ElevatorEvent]

}
