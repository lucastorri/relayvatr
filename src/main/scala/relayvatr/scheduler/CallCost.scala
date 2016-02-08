package relayvatr.scheduler

sealed trait CallCost extends Ordered[CallCost] {

  def canAnswer: Boolean = this != CanNotAnswer

  override def compare(o: CallCost): Int = (this, o) match {
    case (CanNotAnswer, CanNotAnswer) => 0
    case (CanNotAnswer, _) => Int.MaxValue
    case (_, CanNotAnswer) => Int.MinValue
    case (CanAnswer(d1), CanAnswer(d2)) => d1.compareTo(d2)
  }

}

case class CanAnswer(cost: Double) extends CallCost

case object CanNotAnswer extends CallCost
