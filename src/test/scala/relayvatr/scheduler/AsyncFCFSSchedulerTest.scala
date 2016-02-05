package relayvatr.scheduler

class AsyncFCFSSchedulerTest extends FCFSSchedulerTest {

  override def scheduler: FCFSScheduler = new AsyncFCFSScheduler(config)

}
