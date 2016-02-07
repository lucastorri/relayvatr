package relayvatr

import akka.actor.{Terminated, ActorSystem}
import relayvatr.control.{BasicControl, ControlConfig, RangeLimitSensor}
import relayvatr.scheduler.{ClosestElevatorScheduler, SameDirectionElevator}
import relayvatr.user.SingleTrip
import rx.lang.scala.Observable

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

object Main extends App {

  log("Configuring control")

  implicit val exec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = ActorSystem("test-system")

  val firstFloor = 0
  val topFloor = 10
  val elevators = 3
  val totalUsers = 30

  val clock = Observable.interval(500.millis).map(_ => ())
  val config = ControlConfig(elevators, new RangeLimitSensor(firstFloor, topFloor))

  val control = new BasicControl(new ClosestElevatorScheduler(config, clock, new SameDirectionElevator(_, firstFloor)))

  sys.addShutdownHook {
    log("Going down")
    Await.ready(shutdown(), 5.seconds)
  }

  log("Starting user interaction")

  val users = Observable.interval(500.millis).map { _ =>
    val startingFloor = randomFloor
    val destinationFloor = randomFloor
    SingleTrip(startingFloor, destinationFloor)
  }.filter(trip => trip.startingFloor != trip.destinationFloor)

  val arrivals = users.take(totalUsers).map(trip => trip -> trip.on(control)).toBlocking.toList

  log("All users submitted")

  Await.ready(Future.sequence(arrivals.map(_._2)), Duration.Inf)

  log("Users are gone")

  log("Done")

  def randomFloor: Int = Random.nextInt(topFloor + 1)

  def log(msg: String): Unit = println(s"-- $msg")

  def shutdown(): Future[Terminated] = {
    control.shutdown()
    val missingUsers = arrivals.collect { case (trip, result) if !result.isCompleted => trip }
    missingUsers.foreach(trip => log(s"Missing ${trip.name} [ ${trip.startingFloor} -> ${trip.destinationFloor} ]"))
    system.terminate()
  }

}
