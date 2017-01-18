package exceptions

case class ListenerException() extends Exception(Messages.ListenerException)

object Messages {
  val ListenerException = "Issue with streaming listener"
}
