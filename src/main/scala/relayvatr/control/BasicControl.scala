package relayvatr.control

import com.typesafe.scalalogging.StrictLogging
import relayvatr.event._
import relayvatr.exception.InvalidFloorException
import relayvatr.scheduler.Scheduler
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}

class BasicControl(scheduler: Scheduler)(implicit exec: ExecutionContext) extends Control with StrictLogging {

  private val subject = Subject[SystemEvent]()
  private val awaitingPassengers = mutable.HashMap.empty[Int, FloorQueue]
  private val travelingPassengers = mutable.HashMap.empty[String, ElevatorCar]
  private var running = true

  private val queuesHandler: (ElevatorEvent) => Unit = {
    case ElevatorArrived(elevatorId, elevatorFloor, elevatorDirection) =>
      riders(elevatorId).arrive(elevatorFloor)
      queue(elevatorFloor).join(elevatorDirection, new ElevatorProxy(elevatorId))
    case _ =>
  }

  private val queueSubscription = scheduler.events.subscribe(queuesHandler)
  private val relaySubscription = scheduler.events.subscribe(
    ev => { subject.onNext(ev); logger.debug(s"Scheduler event $ev") },
    ex => logger.error("Scheduler error", ex),
    () => logger.debug("Scheduler events closed"))

  override def call(floor: Int, direction: Direction): Future[Elevator] = {
    val promise = Promise[Elevator]()
    try {
      val call = Call(floor, direction)
      scheduler.handle(call)
      queue(floor).add(call, promise)
    } catch {
      case e: InvalidFloorException =>
        logger.error("Invalid floor", e)
        promise.failure(e)
    }
    promise.future
  }

  override def status: ControlStatus = ControlStatus(running, scheduler.status)

  override def events: Observable[SystemEvent] = subject

  override def shutdown(): Future[Unit] = {
    scheduler.shutdown()
    queueSubscription.unsubscribe()
    relaySubscription.unsubscribe()
    subject.onNext(SystemShutdown)
    subject.onCompleted()
    running = false
    Future.successful(())
  }

  private def queue(currentFloor: Int): FloorQueue =
    awaitingPassengers.getOrElseUpdate(currentFloor, new FloorQueue)

  private def riders(elevatorId: String): ElevatorCar =
    travelingPassengers.getOrElseUpdate(elevatorId, new ElevatorCar)

  private class ElevatorProxy(val id: String) extends Elevator {
    override def goTo(floor: Int): Future[Unit] = {
      val promise = Promise[Unit]()
      try {
        scheduler.handle(GoTo(id, floor))
        riders(id).add(promise, floor)
      } catch {
        case e: InvalidFloorException =>
          logger.error("Invalid floor", e)
          promise.failure(e)
      }
      promise.future
    }
  }

  private class FloorQueue {

    private val goingUp = mutable.ListBuffer.empty[Promise[Elevator]]
    private val goingDown = mutable.ListBuffer.empty[Promise[Elevator]]

    def add(call: Call, promise: Promise[Elevator]): Unit = {
      queue(call.direction).append(promise)
    }

    def join(direction: Direction, elevator: Elevator): Unit = {
      val q = queue(direction)
      q.foreach(_.success(elevator))
      q.clear()
    }

    private def queue(direction: Direction): mutable.ListBuffer[Promise[Elevator]] = {
      if (direction == Up) goingUp else goingDown
    }

  }

  private class ElevatorCar {

    private val floors = mutable.HashMap.empty[Int, Set[Promise[Unit]]]

    def add(passenger: Promise[Unit], destinationFloor: Int): Unit = {
      floors(destinationFloor) = floors.getOrElse(destinationFloor, Set.empty) + passenger
    }

    def arrive(elevatorFloor: Int): Unit = {
      for {
        floor <- floors.get(elevatorFloor)
        arrival <- floor
      } arrival.success(())
      floors.remove(elevatorFloor)
    }

  }

}






