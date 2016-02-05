package relayvatr.scheduler

class SyncFCFSSchedulerTest extends FCFSSchedulerTest {

  override def scheduler: FCFSScheduler = new SyncFCFSScheduler(config)

}
