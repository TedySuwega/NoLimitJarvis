package id.nolimit.jarvis.actor

import akka.actor.{ActorLogging, ActorRef, Props}
import id.nolimit.jarvis.dispatcher.DispatcherManager

/**
  * Created by nabilfarras on 22/03/18.
  */
class MasterActor extends SupervisorTrait with ActorLogging {
  implicit val asyncIOEc = DispatcherManager.asyncIOExecutionContext

  override val workerProps: List[NamedProps] = List(
      "web-server" -> Props[WebServerActor]
  )

  override def onReceive = {
    case _ =>
  }

  override def onWorkerDown(ref: ActorRef, props: NamedProps) = {
    println(s"Actor ${ref} is down!")
  }

  override def onWorkerUp(ref: ActorRef, props: NamedProps) = {
    println(s"Actor ${ref} is up!")
  }
}
