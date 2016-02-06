package relayvatr.event

import relayvatr.control.Direction

sealed trait Action

case class GoTo(elevatorId: String, floor: Int) extends Action
case class Call(floor: Int, direction: Direction) extends Action
