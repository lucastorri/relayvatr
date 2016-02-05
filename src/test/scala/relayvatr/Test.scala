package relayvatr

import org.scalatest.{FlatSpec, MustMatchers}

trait Test extends FlatSpec with MustMatchers {

  implicit def exec = scala.concurrent.ExecutionContext.Implicits.global

}
