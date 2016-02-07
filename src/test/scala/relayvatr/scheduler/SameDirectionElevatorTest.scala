package relayvatr.scheduler

import relayvatr.Test
import relayvatr.control.{Up, Down}
import relayvatr.event.{ElevatorPassing, ElevatorArrived, ElevatorLeaving, Call}

class SameDirectionElevatorTest extends Test {

  it must "attend calls if free" in new context {
    elevator.distanceTo(Call(5, Down)).canAnswer must be (true)
    elevator.answer(Call(5, Down))
    elevator.direction must equal (Some(Up))
  }

  it must "announce it is leaving" in new context {
    elevator.answer(Call(5, Down))
    elevator.move() must equal (Some(ElevatorLeaving(id, 0, Up)))
  }

  it must "attend calls if to same direction" in new context {
    elevator.answer(Call(5, Down))

    elevator.distanceTo(Call(3, Up)).canAnswer must be (true)
    elevator.distanceTo(Call(3, Down)).canAnswer must be (false)
  }

  it must "announce if on same floor as the call" in new context {
    elevator.answer(Call(0, Up))
    elevator.move() must equal (Some(ElevatorArrived(id, 0, Up)))
  }

  it must "attend new calls if going on same direction" in new context {
    elevator.answer(Call(5, Down))
    elevator.move()

    elevator.distanceTo(Call(3, Up)).canAnswer must be (true)
    elevator.distanceTo(Call(3, Down)).canAnswer must be (false)
  }

  it must "move between floors" in new context {
    elevator.press(3)
    6.times(elevator.move()) must equal (Seq(
      Some(ElevatorLeaving(id, 0, Up)),
      Some(ElevatorPassing(id, 0, Up)),
      Some(ElevatorPassing(id, 1, Up)),
      Some(ElevatorPassing(id, 2, Up)),
      Some(ElevatorArrived(id, 3, Up)),
      None))
  }

  trait context {
    val id = "fake-1"
    val elevator = new SameDirectionElevator(id)
  }

  implicit class IntTimes(i: Int) {

    def times[T](block: => T): Seq[T] = (0 until i).map(_ => block).toList

  }

}
