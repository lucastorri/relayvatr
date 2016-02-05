package relayvatr.exception

import relayvatr.event.SystemEvent

case class UnexpectedEventException(event: SystemEvent) extends Exception(s"Unexpected event $event")
