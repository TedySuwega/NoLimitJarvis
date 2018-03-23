package id.nolimit.jarvis.actor

import akka.actor.{Actor, ActorRef, Props, Terminated}

/**
  * Created by nabilfarras on 22/03/18.
  */
trait SupervisorTrait extends Actor {

  private var isActive = true

  implicit val tupleToNamedProps = (tuple: (String, Props)) => {
    NamedProps(tuple._2, tuple._1)
  }

  implicit val propsToNamedProps = (props: Props) => NamedProps(props)

  val workerProps : List[NamedProps]

  def onWorkerUp(ref: ActorRef, props: NamedProps) : Unit = {
    println(s"Worker ${ref}  from ${props.props.clazz} is up!")
  }

  def onWorkerDown(ref: ActorRef, props: NamedProps) : Unit = {}

  def onReceive : PartialFunction[Any, Unit] = {
    case _ =>
  }

  def onPreStart() = {}

  private var activeWorkerList = Map.empty[ActorRef, NamedProps]

  final def stopSupervise = {
    isActive = false
    activeWorkerList.keys.foreach { ref =>
      context.stop(ref)
    }
    context stop self
  }

  override final def receive : PartialFunction[Any, Unit] = {

    val baseFunc : PartialFunction[Any, Unit] = {
      case Terminated(ref) =>
        val propsOpt = activeWorkerList.get(ref) map {
          props =>
            onWorkerDown(ref, props)
            props
        }
        activeWorkerList = activeWorkerList - ref
        propsOpt match {
          case Some(props) if isActive =>
            createWorker(props)
          case _ =>
        }
    }
    baseFunc.orElse(onReceive)
  }


  override final def preStart(): Unit = {
    super.preStart()
    workerProps.foreach { createWorker }
    onPreStart()
  }

  private def createWorker(named: NamedProps) = {
    val actor = if(named.name.nonEmpty) {
      val ref = context.actorOf(named.props, named.name)
      ref
    } else context.actorOf(named.props)

    activeWorkerList = activeWorkerList + (actor -> named)
    context watch actor
    onWorkerUp(actor, named)
  }

  override def postStop(): Unit = {
    super.postStop()
    isActive = false
  }

}
