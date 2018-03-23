package id.nolimit.jarvis.route

import akka.actor.ActorContext
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import play.api.libs.json.{JsValue, Json}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._

/**
  * Created by nabilfarras on 23/03/18.
  */
case class WebhookBotRouter()(implicit val context: ActorContext) {
  def getRoute() = {
    path("webhook-bot") {
      (post & entity(as[JsValue])) {
        rawJson =>
          println(s"JsValue ${rawJson}")
          val event = (rawJson \ "type").as[String]
          val space = (rawJson \ "space" \ "type").asOpt[String]
          var text : String = ""

          println(s"EVENT : ${event}")

          if(event == "ADDED_TO_SPACE" && space.nonEmpty){
            text = "Terimakasih sudah mau berteman dengan saya,Mbo Darmi Siap membantu Kalian Semua"
          }
          else if(event == "MESSAGE"){
            val message = (rawJson \ "message" \ "text").get.as[String]
            val sender = (rawJson \ "message" \ "sender" \ "displayName").get.as[String]
            text = s"HALO ${sender} INI MESSAGENYA ${message}"
          }
          complete {
            Json.obj("text" -> text)
          }
      }
    }
  }
}
