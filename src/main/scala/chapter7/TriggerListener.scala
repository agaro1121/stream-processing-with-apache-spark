package chapter7

import org.apache.spark.sql.streaming.StreamingQueryListener

/**
  * Notes: runId will change if a query fails and restarts but
  * id remains the same
  * */
class TriggerListener extends StreamingQueryListener {

  override def onQueryStarted(event: StreamingQueryListener.QueryStartedEvent): Unit =
    println(
      s"""***** Query Started:
         |id=${event.id}
         |name=${event.name}
         |runId=${event.runId}
       """.stripMargin)

  override def onQueryProgress(event: StreamingQueryListener.QueryProgressEvent): Unit =
    println(s"***** Query Progress: ${event.progress.json}")

  override def onQueryTerminated(event: StreamingQueryListener.QueryTerminatedEvent): Unit =
    println(
      s"""***** Query Ended:
         |id=${event.id}
         |runId=${event.runId}
         |exception=${event.exception}
       """.stripMargin)
}

object TriggerListener {
  def apply(): TriggerListener = new TriggerListener()
}