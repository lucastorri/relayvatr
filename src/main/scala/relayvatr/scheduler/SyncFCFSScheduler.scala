package relayvatr.scheduler

import relayvatr.control._
import relayvatr.event._

class SyncFCFSScheduler(val config: ControlSystemConfig) extends FCFSScheduler {

  override def handle(action: Action): Unit = synchronized { act(action) }

  override def shutdown(): Unit = subject.onCompleted()

}
