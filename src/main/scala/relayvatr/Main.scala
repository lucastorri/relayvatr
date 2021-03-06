package relayvatr

import akka.actor.{ActorSystem, Terminated}
import relayvatr.control.{BasicControl, ControlConfig, RangeLimitSensor}
import relayvatr.scheduler.{ClosestElevatorScheduler, SameDirectionElevator}
import relayvatr.user.SingleTrip
import rx.lang.scala.Observable

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

/**
  * Simulates the system by generating random users and submitting them.
  */
object Main extends App {

  log("Configuring control")

  implicit val exec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = ActorSystem("test-system")

  val firstFloor = 0
  val topFloor = 60
  val elevators = 20
  val totalUsers = 1000
  val clockInterval = 200.millis
  val intervalBetweenUsers = 10.millis

  val clock = Observable.interval(clockInterval).map(_ => log("*** *** ***"))
  val config = ControlConfig(elevators, new RangeLimitSensor(firstFloor, topFloor))

  val control = new BasicControl(new ClosestElevatorScheduler(config, clock, new SameDirectionElevator(_, firstFloor)))

  sys.addShutdownHook {
    log("Going down")
    Await.ready(shutdown(), 5.seconds)
  }

  log("Starting user interaction")

  val users = Observable.interval(intervalBetweenUsers).map { _ =>
    val startingFloor = randomFloor
    val destinationFloor = randomFloor
    SingleTrip(startingFloor, destinationFloor)
  }.filter(trip => trip.startingFloor != trip.destinationFloor)

  val arrivals = users.take(totalUsers).map(trip => trip -> trip.on(control)).toBlocking.toList

  log("All users submitted")

  Await.ready(Future.sequence(arrivals.map(_._2)), Duration.Inf)

  log("Users are gone")

  shutdown()

  log("Done")

  def randomFloor: Int = Random.nextInt(topFloor + 1)

  def log(msg: String): Unit = println(s"-- $msg")

  def shutdown(): Future[Terminated] = {
    control.shutdown()
    val missingUsers = arrivals.collect { case (trip, result) if !result.isCompleted => trip }
    missingUsers.foreach(trip => log(s"Missing ${trip.name}"))
    system.terminate()
  }

}
