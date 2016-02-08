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
        log(s"answer($call) : $state -> $newState")
        state = newState
      case None =>
        log(s"answer($call) : $state -> -invalid-")
        throw new CannotHandleCallException()
    }
  }

  override def press(floor: Int): Unit = {
    answer(Call(floor, directionOf(floor)))
  }

  override def distanceTo(call: Call): CallDistance = {
    if (state.answer.isDefinedAt(call)) OnTheWay(math.abs(call.floor - floor) * (state.weight + 1))
    else CanNotAnswer
  }

  override def move(): Option[ElevatorEvent] = {
    val (newState, event) = state.move()
    log(s"move() : $state -> $newState [${event.mkString}]")
    state = newState
    event
  }

  private def log(msg: => String): Unit = {
    logger.trace(msg)
  }

  private def directionOf(toFloor: Int): Direction = {
    if (toFloor > floor) Up else Down
  }

  private def isOnTheWay(state: State, c: Call): Boolean = {
    Set(c.direction, directionOf(c.floor)) == state.direction.toSet
  }

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
      case c if c.floor == floor => Arrived(floor, c.direction)
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
      case c if isOnTheWay(this, c) => copy(floors = floors + c.floor)
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      if (floors.contains(floor)) {
        if (floors.size == 1) Idle(floor) -> Some(ElevatorArrived(id, floor, to))
        else Leaving(copy(floor, to, floors - floor)) -> Some(ElevatorArrived(id, floor, to))
      } else {
        copy(floor = floor + to.count) -> Some(ElevatorPassing(id, floor, to))
      }
    }
  }

  case class GoAndInvert(floor: Int, to: Direction, till: Int) extends State {
    override def direction: Option[Direction] = Some(to)
    override def weight: Int = 1
    override def answer: PartialFunction[Call, State] = PartialFunction.empty
    override def move(): (State, Option[ElevatorEvent]) = {
      if (floor == till) Wait(floor, to.opposite) -> Some(ElevatorArrived(id, floor, to.opposite))
      else copy(floor = floor + to.count) -> Some(ElevatorPassing(id, floor, to))
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
      state -> state.direction.map(ElevatorLeaving(id, floor, _))
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

  case class Arrived(floor: Int, to: Direction, stay: Int = 1) extends State {
    override def direction: Option[Direction] = Some(to)
    override def weight: Int = 0
    override def answer: PartialFunction[Call, State] = {
      case c if isOnTheWay(state, c) => Leaving(GoAndCollect(floor, to, Set(c.floor)))
    }
    override def move(): (State, Option[ElevatorEvent]) = {
      if (stay > 0) copy(stay = stay-1) -> Some(ElevatorArrived(id, floor, to))
      else Idle(floor) -> None
    }
  }

}
