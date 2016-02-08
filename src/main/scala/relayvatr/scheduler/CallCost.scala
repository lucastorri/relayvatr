package relayvatr.scheduler

sealed trait CallCost extends Ordered[CallCost] {

  def canAnswer: Boolean = this != CanNotAnswer

  override def compare(o: CallCost): Int = (this, o) match {
    case (CanNotAnswer, CanNotAnswer) => 0
    case (CanNotAnswer, _) => Int.MaxValue
    case (_, CanNotAnswer) => Int.MinValue
    case (OnTheWay(d1), OnTheWay(d2)) => d1.compareTo(d2)
  }

}

case class OnTheWay(cost: Int) extends CallCost

case object CanNotAnswer extends CallCost
