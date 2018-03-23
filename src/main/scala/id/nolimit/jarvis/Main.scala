package id.nolimit.jarvis

import akka.actor.{ActorSystem, Props}
import id.nolimit.jarvis.actor.MasterActor

/**
  * Created by nabilfarras on 22/03/18.
  */
object Main {
  def main(args: Array[String]) : Unit = {
    val system = ActorSystem("CharonCluster")
    system.actorOf(Props[MasterActor],"master")
  }
}
