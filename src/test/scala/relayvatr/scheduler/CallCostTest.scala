package relayvatr.scheduler

import relayvatr.Test

class CallCostTest extends Test {

  it must "be sortable" in {

    val distances: Seq[CallCost] = Seq(CanNotAnswer, CanAnswer(3), CanAnswer(1), CanAnswer(2), CanNotAnswer)

    distances.sorted must equal (Seq(CanAnswer(1), CanAnswer(2), CanAnswer(3), CanNotAnswer, CanNotAnswer))
  }

}
