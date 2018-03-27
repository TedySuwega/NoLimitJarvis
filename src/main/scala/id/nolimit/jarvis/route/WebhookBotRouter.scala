package id.nolimit.jarvis.route

import akka.actor.ActorContext
import akka.http.scaladsl.server.Directives.{as, complete, entity, onComplete, path, post, reject}
import play.api.libs.json.{JsValue, Json}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import id.nolimit.jarvis.actor.KnowledgeProcessingActor.{askKnowledge, resultStates}
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._

import scala.util.{Failure, Success}

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
            complete {
              Json.obj("text" -> text)
            }
          }
          else {
            val message = (rawJson \ "message" \ "text").get.as[String].replace("@NoLimitJarvis", "")
            val sender = (rawJson \ "message" \ "sender" \ "displayName").get.as[String]
            val thread = (rawJson \ "message" \ "thread" \ "name").get.as[String]

            val knowledgeActor = context.actorSelection("/user/master/knowledge-actor")
            implicit val timeout = Timeout(3 minutes)
            onComplete(knowledgeActor ? askKnowledge(message, thread, sender)){
              case Success(item: resultStates) =>
                println(
                  s"""
                    |RESULT : ${item.result}
                  """.stripMargin)
                complete{
                  Json.obj("text" -> item.result)
                }
              case Failure(reason) =>
                complete(s"ERROR ${reason}")
              case _ =>
                println("case")
                complete("case route kosng")
            }
          }
      }
    }
  }
}
