package relayvatr.control

import java.util.concurrent.atomic.AtomicLong

import relayvatr.event._
import relayvatr.exception.{InvalidFloorException, UnexpectedEventException}
import relayvatr.scheduler.SystemScheduler
import rx.lang.scala.{Observable, Subject}

import scala.concurrent.{ExecutionContext, Future, Promise}

class BasicControl(scheduler: SystemScheduler)(implicit exec: ExecutionContext) extends Control {

  private val counter = new AtomicLong()

  private val subject = Subject[SystemEvent]()
  scheduler.events.map(_.event).subscribe(ev => subject.onNext(ev))

  override def call(currentFloor: Int, direction: Direction): Future[Elevator] = {
    val promise = Promise[Elevator]()
    val id = subscribe {
      case ev: ElevatorArrived => promise.success(new ElevatorProxy(ev.elevatorId))
      case ev: InvalidFloor => promise.failure(InvalidFloorException(ev.floor))
      case ev => promise.failure(UnexpectedEventException(ev))
    }
    scheduler.handle(Call(id, currentFloor, direction))
    promise.future
  }

  override def events: Observable[SystemEvent] = subject

  override def shutdown(): Future[Unit] = {
    scheduler.shutdown()
    subject.onNext(SystemShutdown)
    subject.onCompleted()
    Future.successful(())
  }

  private def subscribe(pf: PartialFunction[SystemEvent, Unit]): Long = {
    val id = counter.getAndIncrement()
    val onNext: (ActionEvent) => Unit = {
      case ActionEvent(action, event) if id == action.id => pf(event)
      case _ =>
    }
    scheduler.events.subscribe(onNext)
    id
  }

  private class ElevatorProxy(val id: String) extends Elevator {
    override def goTo(n: Int): Future[Unit] = {
      val promise = Promise[Unit]()
      val actionId = subscribe {
        case ev: ElevatorArrived => promise.success(())
        case ev: InvalidFloor => promise.failure(InvalidFloorException(ev.floor))
        case ev => promise.failure(new UnexpectedEventException(ev))
      }
      scheduler.handle(GoTo(actionId, id, n))
      promise.future
    }
  }

}






