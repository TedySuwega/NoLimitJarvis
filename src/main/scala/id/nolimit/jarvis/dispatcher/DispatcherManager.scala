package id.nolimit.jarvis.dispatcher

import java.util.concurrent.Executors

/**
  * Created by nabilfarras on 22/03/18.
  */
object DispatcherManager {

  private object executors {
    val cachedThreadPool = Executors.newCachedThreadPool()
    val fixedThreadPool = Executors.newFixedThreadPool(200)
    val hugeFixedThreadPool = Executors.newFixedThreadPool(200)
    val hugeReplyThreadPool = Executors.newFixedThreadPool(100)
    val schedulerPostThreadPool = Executors.newFixedThreadPool(10)
  }

  val blockingIOExecutionContext = scala.concurrent.ExecutionContext.fromExecutor(executors.fixedThreadPool)
  val asyncIOExecutionContext = scala.concurrent.ExecutionContext.fromExecutor(executors.hugeFixedThreadPool)
  val blockingIOReplyExecutionContext = scala.concurrent.ExecutionContext.fromExecutor(executors.hugeReplyThreadPool)
  val schedulerIOExecutionContext = scala.concurrent.ExecutionContext.fromExecutor(executors.schedulerPostThreadPool)
}
