package relayvatr.scheduler

import relayvatr.Test
import relayvatr.control.{Up, Down}
import relayvatr.event.{ElevatorPassing, ElevatorArrived, ElevatorLeaving, Call}
import relayvatr.exception.CannotHandleCallException

class SameDirectionElevatorTest extends Test {

  it must "always attend if free" in new context {
    elevator.answer(Call(7, Up))
  }

  it must "throw an error if attending a call that cannot be answered" in new context {
    elevator.answer(Call(4, Up))

    an [CannotHandleCallException] should be thrownBy {
      elevator.answer(Call(4, Down))
    }
  }

  it must "attend two calls on the same floor and direction" in new context {
    elevator.answer(Call(4, Up))

    elevator.distanceTo(Call(4, Up)).canAnswer must be (true)
    elevator.distanceTo(Call(4, Down)).canAnswer must be (false)
  }

  it must "attend calls going up if calling user is on a higher floor and will go up" in new context {
    elevator.answer(Call(4, Up))

    elevator.distanceTo(Call(6, Up)).canAnswer must be (true)
    elevator.distanceTo(Call(6, Down)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Down)).canAnswer must be (false)
  }

  it must "go straight to an upper floor if needs to change direction later" in new context {
    elevator.answer(Call(4, Down))

    elevator.distanceTo(Call(6, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(6, Down)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Down)).canAnswer must be (false)
  }

  it must "go straight to a lower floor if needs to change direction later" in new context {
    elevator.answer(Call(2, Up))

    elevator.distanceTo(Call(6, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(6, Down)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Down)).canAnswer must be (false)
  }

  it must "attend calls going down if calling user is on a lower floor and will go down" in new context {
    elevator.answer(Call(2, Down))

    elevator.distanceTo(Call(6, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(6, Down)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Up)).canAnswer must be (false)
    elevator.distanceTo(Call(1, Down)).canAnswer must be (true)
  }

  it must "remain idle if idle" in new context {
    elevator.move() must equal (Option.empty)
  }

  it must "announce arrival down if idle on call floor" in new context {
    elevator.answer(Call(3, Down))

    elevator.move() must equal (Option(ElevatorArrived(id, 3, Down)))
    elevator.press(1)
    elevator.move() must equal(Option(ElevatorLeaving(id, 3, Down)))
    elevator.move() must equal(Option(ElevatorPassing(id, 2, Down)))
    elevator.move() must equal(Option(ElevatorArrived(id, 1, Down)))
    elevator.move() must equal (Option.empty)
  }

  it must "announce arrival up if idle on call floor" in new context {
    elevator.answer(Call(3, Up))

    elevator.move() must equal (Option(ElevatorArrived(id, 3, Up)))
    elevator.press(5)
    elevator.move() must equal(Option(ElevatorLeaving(id, 3, Up)))
    elevator.move() must equal(Option(ElevatorPassing(id, 4, Up)))
    elevator.move() must equal(Option(ElevatorArrived(id, 5, Up)))
    elevator.move() must equal (Option.empty)
  }

  it must "go straight to a floor if will invert direction later" in new context {
    elevator.answer(Call(5, Down))
    elevator.move() must equal (Option(ElevatorLeaving(id, 3, Up)))
    elevator.move() must equal (Option(ElevatorPassing(id, 4, Up)))
    elevator.move() must equal (Option(ElevatorArrived(id, 5, Down)))
    elevator.press(4)
    elevator.move() must equal (Option(ElevatorLeaving(id, 5, Down)))
    elevator.move() must equal (Option(ElevatorArrived(id, 4, Down)))
    elevator.move() must equal (Option.empty)
  }

  it must "pick up other calls if they are on the same direction" in new context {
    elevator.answer(Call(4, Up))
    elevator.answer(Call(5, Up))
    elevator.move() must equal (Option(ElevatorLeaving(id, 3, Up)))
    elevator.move() must equal (Option(ElevatorArrived(id, 4, Up)))
    elevator.answer(Call(6, Up))
    elevator.move() must equal (Option(ElevatorLeaving(id, 4, Up)))
    elevator.move() must equal (Option(ElevatorArrived(id, 5, Up)))
    elevator.move() must equal (Option(ElevatorLeaving(id, 5, Up)))
    elevator.move() must equal (Option(ElevatorArrived(id, 6, Up)))
    elevator.move() must equal (Option.empty)
  }

  trait context {
    val id = "fake-1"
    val elevator = new SameDirectionElevator(id, initialFloor = 3)
  }

  implicit class IntTimes(i: Int) {

    def times[T](block: => T): Seq[T] = (0 until i).map(_ => block).toList

  }

}
