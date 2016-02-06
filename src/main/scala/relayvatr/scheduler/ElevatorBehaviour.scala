package relayvatr.scheduler

import relayvatr.event.{ElevatorEvent, Call}

trait ElevatorBehaviour {

  def id: String

  def answer(call: Call): Unit

  def press(floor: Int): Unit

  def distanceOf(call: Call): CallDistance

  def move(): Option[ElevatorEvent]

}
