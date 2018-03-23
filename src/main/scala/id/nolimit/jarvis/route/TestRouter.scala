package id.nolimit.jarvis.route

import akka.actor.ActorContext
import akka.http.scaladsl.server.Directives.{complete, get, path}

/**
  * Created by nabilfarras on 23/03/18.
  */
case class TestRouter()(implicit val context: ActorContext) {
  def getRoute() = {
    path("test") {
      get {
        complete("TEST")
      }
    }
  }
}
