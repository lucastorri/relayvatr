package relayvatr.control

import relayvatr.event._
import relayvatr.exception.InvalidFloorException
import relayvatr.scheduler.Scheduler
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}

class BasicControl(scheduler: Scheduler)(implicit exec: ExecutionContext) extends Control {

  private val subject = Subject[SystemEvent]()
  private val awaitingPassengers = mutable.HashMap.empty[Int, FloorQueue]
  private val travelingPassengers = mutable.HashMap.empty[String, ElevatorSet]

  private val queuesHandler: (ElevatorEvent) => Unit = {
    case ElevatorArrived(elevatorId, elevatorFloor, elevatorDirection) =>
      riders(elevatorId).arrive(elevatorFloor)
      queue(elevatorFloor).join(elevatorDirection, new ElevatorProxy(elevatorId))
    case _ =>
  }

  private val queueSubscription = scheduler.events.subscribe(queuesHandler)
  private val relaySubscription = scheduler.events.subscribe(ev => subject.onNext(ev))

  override def call(floor: Int, direction: Direction): Future[Elevator] = {
    val promise = Promise[Elevator]()
    try {
      val call = Call(floor, direction)
      scheduler.handle(call)
      queue(floor).add(call, promise)
    } catch {
      case e: InvalidFloorException => promise.failure(e)
    }
    promise.future
  }

  private def queue(currentFloor: Int): FloorQueue =
    awaitingPassengers.getOrElseUpdate(currentFloor, new FloorQueue)

  private def riders(elevatorId: String): ElevatorSet =
    travelingPassengers.getOrElseUpdate(elevatorId, new ElevatorSet)

  override def events: Observable[SystemEvent] = subject

  override def shutdown(): Future[Unit] = {
    scheduler.shutdown()
    queueSubscription.unsubscribe()
    relaySubscription.unsubscribe()
    subject.onNext(SystemShutdown)
    subject.onCompleted()
    Future.successful(())
  }

  private class ElevatorProxy(val id: String) extends Elevator {
    override def goTo(floor: Int): Future[Unit] = {
      val promise = Promise[Unit]()
      try {
        scheduler.handle(GoTo(id, floor))
        riders(id).add(promise, floor)
      } catch {
        case e: InvalidFloorException => promise.failure(e)
      }
      promise.future
    }
  }

  class FloorQueue {
    private val goingUp = mutable.ListBuffer.empty[Promise[Elevator]]
    private val goingDown = mutable.ListBuffer.empty[Promise[Elevator]]

    def add(call: Call, promise: Promise[Elevator]): Unit = {
      queue(call.direction) += promise
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

  class ElevatorSet {

    private val floors = mutable.HashMap.empty[Int, Set[Promise[Unit]]]

    def arrive(elevatorFloor: Int): Unit = {
      floors(elevatorFloor).foreach(_.success(()))
      floors.remove(elevatorFloor)
    }

    def add(passenger: Promise[Unit], destinationFloor: Int): Unit = {
      floors(destinationFloor) = floors.getOrElse(destinationFloor, Set.empty) + passenger
    }

  }

}






