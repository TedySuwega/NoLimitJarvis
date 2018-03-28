package id.nolimit.jarvis.actor

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import com.typesafe.config.ConfigFactory
import id.nolimit.jarvis.actor.KnowledgeProcessingActor._
import id.nolimit.jarvis.util.StringUtil
import play.api.libs.json.Json
import akka.http.scaladsl.{Http => Client}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import id.nolimit.jarvis.dispatcher.DispatcherManager
import scala.util.{Failure, Success}

/**
  * Created by nabilfarras on 24/03/18.
  */

class KnowledgeProcessingActor extends Actor with ActorLogging{

  private var threadStates = Map.empty[String,Map[String,states]]
  private var botStates = Map.empty[String, thread]
  private var dmStates = Map.empty[String, thread]
  private val config = ConfigFactory.load()
  private val chatbotUrl = config.getConfig("chatbot")

  override def receive = {
    case askKnowledge(question, thread, senderName,typeMessage) =>

      println(
        s"""
          |QUESTION : ${question}
          |THREAD : ${thread}
          |SENDERNAME : ${senderName}
        """.stripMargin)

      if(question.contains("Tolong catetin dong") && typeMessage == "ROOM"){
        threadStates = threadStates + (thread -> Map(StringUtil.randomString() -> states("Initial")))
        sender() ! resultStates("Oke Selalu Siap saya")
      }
      else if(question.contains("Minta Rekap") && typeMessage == "ROOM"){
        if(threadStates.isDefinedAt(thread)){
          val result = threadStates(thread).foldLeft[List[String]](List()) {
            (a,b) =>
              if(!b._2.state.contains("Initial")){
                a :+ b._2.state
              }
              else{
                a :+ ""
              }
          }
          sender() ! resultStates(s"Ini rekapnya : ${result.mkString("\n")}")
        }
        else {
          sender() ! resultStates("Bilang dulu dong 'Tolong catetin dong'")
        }
      }
      else if(question.contains("/care-chatbot mau tanya")) {
        if(typeMessage == "ROOM") {
          println("ROOM MODE")
          if (!threadStates.isDefinedAt(thread)) {
            val threadSender = thread
            val requester = sender()
            implicit val materializer = ActorMaterializer()
            implicit val asyncIOEc = DispatcherManager.asyncIOExecutionContext
            implicit val system = ActorSystem()
            val obj = Json.obj(
              "nama" -> senderName,
              "idbot" -> "1"
            )
            val entity = HttpEntity(ContentTypes.`application/json`, obj.toString())
            val request = HttpRequest(
              uri = chatbotUrl.getString("urlSession"),
              entity = entity,
              method = HttpMethods.POST)

            Client()
              .singleRequest(request)
              .flatMap { response =>
                val statusCode = response.status
                if (statusCode.isFailure()) {
                  throw new Exception(statusCode.reason())
                } else {
                  response
                    .entity
                    .dataBytes
                    .runFold(ByteString(""))(_ ++ _)
                    .map(_.utf8String)
                }
              }.onComplete {
              case Success(item) =>
                val result = Json.parse(item)
                println(s"item : ${Json.parse(item)}")
                println(s"TOKEN : ${(result \ "token").as[String]}")
                println(s"OUTPUT : ${(result \ "output").as[String]}")
                val token = (result \ "token").as[String]
                val output = (result \ "output").as[String]
                botStates = botStates + (token -> new thread(senderName, threadSender))
                requester ! resultStates(output)
              case Failure(reason) =>
                println(s"reason ${reason}")
            }
          }
        }
        else if(typeMessage == "DM"){
          println("DM MODE")
          val threadSender = thread
          val requester = sender()
          implicit val materializer = ActorMaterializer()
          implicit val asyncIOEc = DispatcherManager.asyncIOExecutionContext
          implicit val system = ActorSystem()
          val obj = Json.obj(
            "nama" -> senderName,
            "idbot" -> "1"
          )
          val entity = HttpEntity(ContentTypes.`application/json`, obj.toString())
          val request = HttpRequest(
            uri = chatbotUrl.getString("urlSession"),
            entity = entity,
            method = HttpMethods.POST)

          Client()
            .singleRequest(request)
            .flatMap { response =>
              val statusCode = response.status
              if (statusCode.isFailure()) {
                throw new Exception(statusCode.reason())
              } else {
                response
                  .entity
                  .dataBytes
                  .runFold(ByteString(""))(_ ++ _)
                  .map(_.utf8String)
              }
            }.onComplete {
            case Success(item) =>
              val result = Json.parse(item)
              println(s"item : ${Json.parse(item)}")
              println(s"TOKEN : ${(result \ "token").as[String]}")
              println(s"OUTPUT : ${(result \ "output").as[String]}")
              val token = (result \ "token").as[String]
              val output = (result \ "output").as[String]
              dmStates = dmStates + (senderName -> new thread(senderName, token))
              requester ! resultStates(output)
            case Failure(reason) =>
              println(s"reason ${reason}")
          }
        }
      }
      else if (question.contains("care-chatbot")){
        val requester  = sender()
        if(typeMessage == "ROOM"){
          println("ROOM MODE")
          val definedBot = botStates.find{
            item => item._2.name == senderName && item._2.threadType == thread
          }
          definedBot match {
            case Some(item) =>
              implicit val materializer = ActorMaterializer()
              implicit val asyncIOEc = DispatcherManager.asyncIOExecutionContext
              implicit val system = ActorSystem()
              val input = question.split(" ")

              val obj = Json.obj(
                "input" -> input(2),
                "token" -> item._1
              )
              val entity = HttpEntity(ContentTypes.`application/json`, obj.toString())
              val request = HttpRequest(
                uri = chatbotUrl.getString("urlChat"),
                entity = entity,
                method = HttpMethods.POST)
              Client()
                .singleRequest(request)
                .flatMap{ response =>
                  val statusCode = response.status
                  if (statusCode.isFailure()) {
                    throw new Exception(statusCode.reason())
                  } else
                  {
                    response
                      .entity
                      .dataBytes
                      .runFold(ByteString(""))(_++_)
                      .map(_.utf8String)
                  }
                }.onComplete{
                case Success(item) =>
                  val result = Json.parse(item)
                  val output = (result \ "output").as[String]
                  requester ! resultStates(output)
                case Failure(reason) =>
                  println(s"reason ${reason}")
              }
            case _ =>
              requester ! resultStates("Kodenya dulu dong /care-chatbot mau tanya")
          }
        }
        else if(typeMessage == "DM"){
          println("DM MODE")
          println(s"DMSTATES : ${dmStates}")

          if(dmStates.isDefinedAt(senderName)){
            val dmState = dmStates(senderName)
            implicit val materializer = ActorMaterializer()
            implicit val asyncIOEc = DispatcherManager.asyncIOExecutionContext
            implicit val system = ActorSystem()
            val input = question.split(" ")
            val obj = Json.obj(
              "input" -> input(2),
              "token" -> dmState.threadType
            )
            val entity = HttpEntity(ContentTypes.`application/json`, obj.toString())
            val request = HttpRequest(
              uri = chatbotUrl.getString("urlChat"),
              entity = entity,
              method = HttpMethods.POST)
            Client()
              .singleRequest(request)
              .flatMap{ response =>
                val statusCode = response.status
                if (statusCode.isFailure()) {
                  throw new Exception(statusCode.reason())
                } else
                {
                  response
                    .entity
                    .dataBytes
                    .runFold(ByteString(""))(_++_)
                    .map(_.utf8String)
                }
              }.onComplete{
              case Success(item) =>
                val result = Json.parse(item)
                val output = (result \ "output").as[String]
                requester ! resultStates(output)
              case Failure(reason) =>
                println(s"reason ${reason}")
            }
          }
          else{
            requester ! resultStates("Kodenya dulu dong /care-chatbot mau tanya")
          }
        }
      }
      else if(question.contains("/help")){
        val requester = sender()
        val text =
          """
            |1. /care-chatbot mau tanya
            |2. /care-chatbot
            |3. Tolong catetin dong
            |4. Minta Rekap
          """.stripMargin
        requester ! resultStates(text)
      }
      else if(question.contains("Siapa yang paling ganteng di NoLimit ?")){
        val text = if(senderName.contains("NABIL")){
          "NABIL FARRAS"
        }
        else{
          s"NABIL FARRAS - Diakui Oleh ${senderName}"
        }
        sender() ! resultStates(text)
      }
      else {
        if(threadStates.isDefinedAt(thread)){
          val oldData = threadStates(thread)
          val newData = oldData + (StringUtil.randomString() -> states(senderName))
          threadStates = threadStates + (thread -> newData)
          sender() ! resultStates(s"Noted ${senderName}")
        }
        else{
          sender() ! resultStates(s"Halo ${senderName} ada yang bisa dibantu ?")
        }
      }
    case _ =>
  }

  override def preStart(): Unit = {
    super.preStart()
    log.info("Knowledge Processing Actor is up")
  }
}

object KnowledgeProcessingActor {

  case class askKnowledge(
                         question : String,
                         thread : String,
                         senderName : String,
                         typeMessage : String
                         )
  case class states(
                  state : String
                  )
  case class resultStates(
                         result : String
                         )
  case class thread(
                   name : String,
                   threadType : String
                   )
}
