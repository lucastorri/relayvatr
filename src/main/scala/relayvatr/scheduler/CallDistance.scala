package relayvatr.scheduler

sealed trait CallDistance extends Ordered[CallDistance] {
  def canAnswer: Boolean = this != CanNotAnswer
  override def compare(o: CallDistance): Int = (this, o) match {
    case (CanNotAnswer, CanNotAnswer) => 0
    case (CanNotAnswer, _) => Int.MaxValue
    case (_, CanNotAnswer) => Int.MinValue
    case (OnTheWay(d1), OnTheWay(d2)) => d1.compareTo(d2)
  }
}

case class OnTheWay(distance: Int) extends CallDistance

case object CanNotAnswer extends CallDistance
