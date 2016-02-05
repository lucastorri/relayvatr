package relayvatr.scheduler

import akka.actor.{Actor, ActorSystem, Props}
import relayvatr.control.ControlConfig
import relayvatr.event._

class AsyncFCFSScheduler(val config: ControlConfig)(implicit system: ActorSystem) extends FCFSScheduler {

  private val handler = system.actorOf(Props(new Handler))

  override def handle(action: Action): Unit = handler ! action

  override def shutdown(): Unit = {
    system.stop(handler)
    subject.onCompleted()
  }

  private class Handler extends Actor {
    override def receive: Receive = { case a: Action => act(a) }
  }

}
