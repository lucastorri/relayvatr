package relayvatr

import akka.actor.ActorSystem
import relayvatr.control.{BasicControl, ControlConfig, RangeLimitSensor}
import relayvatr.scheduler.{ClosestElevatorScheduler, SameDirectionElevator}
import relayvatr.user.SingleTrip
import rx.lang.scala.Observable

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

object Main extends App {

  implicit val exec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = ActorSystem("test-system")

  val firstFloor = 0
  val topFloor = 10
  val elevators = 3
  val totalUsers = 30

  val clock = Observable.interval(500.millis).map(_ => ())
  val config = ControlConfig(elevators, new RangeLimitSensor(firstFloor, topFloor))

  val control = new BasicControl(new ClosestElevatorScheduler(config, clock, new SameDirectionElevator(_, firstFloor)))

  val users = Observable.interval(500.millis).map { _ =>
    val startingFloor = randomFloor
    val destinationFloor = randomFloor
    SingleTrip(startingFloor, destinationFloor)
  }

  val arrivals = users.take(totalUsers).map(_.on(control)).toBlocking.toList
  Await.ready(Future.sequence(arrivals), Duration.Inf)

  def randomFloor: Int = Random.nextInt(topFloor + 1)

}
