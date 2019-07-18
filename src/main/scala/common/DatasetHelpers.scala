package common

import java.sql.Timestamp

object DatasetHelpers {
  val datasetDirectory = "/tmp/nasa_dataset_july_1995"
  final case class WebLog(host: String, timestamp: Timestamp, request: String, http_reply: Int, bytes: Long)
}