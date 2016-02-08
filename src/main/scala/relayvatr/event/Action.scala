package relayvatr.event

import relayvatr.control.Direction

/**
  * An object that describes actions performed by users.
  */
sealed trait Action

/**
  * A user `Action` for a specific elevator to move to a given floor.
  * @param elevatorId the elevator to perform the action
  * @param floor the floor to go to
  */
case class GoTo(elevatorId: String, floor: Int) extends Action

/**
  * A user `Action` requesting any elevator to pick him/her up.
  * @param floor the floor where the user is
  * @param direction the direction the user intends to go
  */
case class Call(floor: Int, direction: Direction) extends Action
