package relayvatr.event

import relayvatr.control.Direction

sealed trait Action { def id: Long }

case class GoTo(id: Long, elevatorId: String, floor: Int) extends Action
case class Call(id: Long, floor: Int, direction: Direction) extends Action
