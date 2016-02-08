package relayvatr.scheduler

import com.typesafe.scalalogging.StrictLogging
import relayvatr.control._
import relayvatr.event._
import relayvatr.exception.CannotHandleCallException

/**
  * if user called from an upper floor:
  *   - and user wants to go down: will go straight and only when descending get others on the way down
  *   - and user wants to go up: will collect anyone on the way that is going up
  * if user called from an lower floor:
  *   - and user wants to go down: will collect anyone on the way that is going down
  *   - and user wants to go up: will go straight and only when ascending get others on the way up
  * if user called from the current floor:
  *   - will go on the direction requested by the first user
  */
class SameDirectionElevator(
  val id: String,
  initialFloor: Int = 0
) extends ElevatorBehaviour with StrictLogging {

  private var state: State = Idle(initialFloor)

  override def floor: Int = state.floor

  override def direction: Option[Direction] = state.direction

  override def answer(call: Call): Unit = {
    state.answer.lift(call) match {
      case Some(newState) =>
        log(s"State change on $id: answer($call) = $state -> $newState")
        state = newState
      case None =>
        log(s"State change on $id: answer($call) = $state -> -invalid-")
        throw new CannotHandleCallException()
    }
  }

  override def press(floor: Int): Unit = {
    answer(Call(floor, directionOf(floor)))
  }

  override def distanceTo(call: Call): CallCost = {
    if (state.answer.isDefinedAt(call)) CanAnswer(math.abs(call.floor - floor) * (state.weight + 1))
    else CanNotAnswer
  }

  override def move(): Option[ElevatorEvent] = {
    val (newState, event) = state.move()
    log(s"State change on $id: move() = $state -> $newState [${event.mkString}]")
    state = newState
    event
  }

  private def log(msg: => String): Unit = {
    logger.trace(msg)
  }

  private def directionOf(anotherFloor: Int): Direction = {
    if (anotherFloor > floor) Up else Down
  }

  private def isOnTheWay(state: State, c: Call): Boolean = {
    Set(c.direction, directionOf(c.floor)) == state.direction.toSet
  }

  private def arrive(direction: Direction): Option[ElevatorArrived] = Some(ElevatorArrived(id, floor, direction))

  private def leave(direction: Direction): Option[ElevatorLeaving] = Some(ElevatorLeaving(id, floor, direction))

  private def pass(direction: Direction): Option[ElevatorPassing] = Some(ElevatorPassing(id, floor, direction))

  trait State {
    def floor: Int
    def direction: Option[Direction]
    def weight: Int
    def answer: PartialFunction[Call, State]
    def move(): (State, Option[ElevatorEvent])
  }

  case class Idle(floor: Int) extends State {
    override def direction: Option[Direction] = None
    override def weight: Int = 0
    override def answer: PartialFunction[Call, State] = {
      case c if c.floor == floor => Arrived(floor, c.direction, Set.empty)
      case c if directionOf(c.floor) == c.direction => Leaving(GoAndCollect(floor, c.direction, Set(c.floor)))
      case c => Leaving(GoAndInvert(floor, directionOf(c.floor), c.floor))
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      this -> None
    }
  }

  case class GoAndCollect(floor: Int, to: Direction, floors: Set[Int]) extends State {
    override def direction: Option[Direction] = Some(to)
    override def weight: Int = floors.size
    override def answer: PartialFunction[Call, State] = {
      case c if floor != c.floor && isOnTheWay(this, c) => copy(floors = floors + c.floor)
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      if (floors.contains(floor)) {
        if (floors.size == 1) Idle(floor) -> arrive(to)
        else Leaving(copy(floor, to, floors - floor)) -> arrive(to)
      } else {
        copy(floor = floor + to.count) -> pass(to)
      }
    }
  }

  case class GoAndInvert(floor: Int, to: Direction, till: Int) extends State {
    override def direction: Option[Direction] = Some(to)
    override def weight: Int = 1
    override def answer: PartialFunction[Call, State] = PartialFunction.empty
    override def move(): (State, Option[ElevatorEvent]) = {
      if (floor == till) Wait(floor, to.opposite) -> arrive(to.opposite)
      else copy(floor = floor + to.count) -> pass(to)
    }
  }

  case class Leaving(next: State) extends State {
    override def floor: Int = next.floor
    override def direction: Option[Direction] = next.direction
    override def weight: Int = next.weight
    override def answer: PartialFunction[Call, State] = next.answer.andThen {
      case l: Leaving => l
      case other => Leaving(other)
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      val (state, _) = next.move()
      state -> state.direction.flatMap(leave)
    }
  }

  case class Wait(floor: Int, to: Direction) extends State {
    override def direction: Option[Direction] = Some(to)
    override def weight: Int = 0
    override def answer: PartialFunction[Call, State] = {
      case c if isOnTheWay(state, c) => Leaving(GoAndCollect(floor, to, Set(c.floor)))
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      Idle(floor) -> None
    }
  }

  case class Arrived(floor: Int, to: Direction, floors: Set[Int], stay: Int = 1) extends State {
    override def direction: Option[Direction] = Some(to)
    override def weight: Int = 0
    override def answer: PartialFunction[Call, State] = {
      case c if isOnTheWay(state, c) => copy(floors = floors + c.floor)
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      if (stay > 0) copy(stay = stay-1) -> arrive(to)
      else if (floors.nonEmpty) Leaving(GoAndCollect(floor, to, floors - floor)).move()
      else Idle(floor) -> None
    }
  }

}
