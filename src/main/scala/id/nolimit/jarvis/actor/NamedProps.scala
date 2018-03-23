package id.nolimit.jarvis.actor

import akka.actor.Props

/**
  * Created by nabilfarras on 22/03/18.
  */
case class NamedProps(props: Props, name : String = "")
