package id.nolimit.jarvis.actor

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.{complete, encodeResponse, handleExceptions, handleRejections, pathSingleSlash, redirect}
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.typesafe.config.ConfigFactory
import id.nolimit.jarvis.dispatcher.DispatcherManager
import id.nolimit.jarvis.route.{TestRouter, WebhookBotRouter}
import akka.http.scaladsl.server.Directives._
/**
  * Created by nabilfarras on 23/03/18.
  */
class WebServerActor extends Actor with ActorLogging{

  implicit val asyncIOEc = DispatcherManager.asyncIOExecutionContext
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  private val config = ConfigFactory.load()
  private val webServerConfig = config.getConfig("webserver")
  private val webServerPort = webServerConfig.getInt("port")

  private def createRoute = {
    val rejectionHandler = corsRejectionHandler withFallback RejectionHandler.newBuilder().handleNotFound {
      redirect(Uri("/"), StatusCodes.SeeOther)
    }.result()

    val exceptionHandler = ExceptionHandler {
      case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
    }
    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)
    val defaultSettings = CorsSettings.defaultSettings.copy(exposedHeaders = List(
      "Cache-Control",
      "Content-Language",
      "Content-Type",
      "Expires",
      "Last-Modified",
      "Pragma",
      "Set-Authorization",
      "Set-Refresh-Token"
    ))

    val route =
      TestRouter().getRoute() ~
      WebhookBotRouter().getRoute() ~
      pathSingleSlash {
        complete("Welcome to NoLimit Jarvis")
      }
      encodeResponse{
        handleErrors{
          cors(defaultSettings) { route }
        }
      }
  }


  override def receive = {
    case _ =>
  }

  override def preStart(): Unit = {
    super.preStart()
    val httpInstance = Http()
    httpInstance.bindAndHandle(
      createRoute,
      interface = "0.0.0.0",
      port = webServerPort
    ).map {
      result =>
        log.info(s"Successfully bind on ${result.localAddress}")
    }
  }
}
