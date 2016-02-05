package relayvatr

import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, FlatSpec, MustMatchers}

trait Test extends FlatSpec with MustMatchers with BeforeAndAfterAll {

  implicit val exec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = ActorSystem("test-system")

  override protected def afterAll(): Unit = {
    system.terminate()
  }

}
