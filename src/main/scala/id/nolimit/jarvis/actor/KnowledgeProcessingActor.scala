package id.nolimit.jarvis.actor

import akka.actor.{Actor, ActorLogging}
import id.nolimit.jarvis.actor.KnowledgeProcessingActor.{askKnowledge, resultStates, states}
import id.nolimit.jarvis.util.StringUtil

/**
  * Created by nabilfarras on 24/03/18.
  */

class KnowledgeProcessingActor extends Actor with ActorLogging{

  private var threadStates = Map.empty[String,Map[String,states]]

  override def receive = {
    case askKnowledge(question, thread, senderName) =>
      if(question.contains("Tolong catetin dong")){
        threadStates = threadStates + (thread -> Map(StringUtil.randomString() -> states("Initial")))
        sender() ! resultStates("Oke Selalu Siap saya")
      }
      else if(question.contains("Minta Rekap")){
        if(threadStates.isDefinedAt(thread)){
          val result = threadStates(thread).foldLeft[List[String]](List()) {
            (a,b) =>
              a :+ b._2.state
          }

          sender() ! resultStates(result.mkString("/n"))
        }
        else {
          sender() ! resultStates("Bilang dulu dong 'Tolong catetin dong'")
        }
      }
      else if(question == "Siapa yang paling ganteng di NoLimit ?"){
        sender() ! resultStates(s"NABIL FARRAS : Pengirim ${senderName}")
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
                         senderName : String
                         )
  case class states(
                  state : String
                  )
  case class resultStates(
                         result : String
                         )
}
