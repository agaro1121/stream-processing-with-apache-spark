package chapter7

import java.sql.Timestamp
import java.util.concurrent.{Executors, TimeUnit}

import common.SparkBoilerplate
import org.apache.spark.sql.{DataFrame, Dataset, Encoders}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{IntegerType, StructType}

final case class WebLog(host: String, timestamp: Timestamp, request: String, http_reply: Int, bytes: Long)

object NasaDatasetStreamed extends SparkBoilerplate {

  import spark.implicits._

  /**
    * Need to register any StreamingQueryListeners
    * */
  spark.streams.addListener(TriggerListener())

  def main(args: Array[String]): Unit = {

    val stream = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()

    val webLogSchema: StructType = Encoders.product[WebLog].schema
    val jsonStream: DataFrame = stream.select(from_json($"value", webLogSchema) as "record")
    val webLogStream: Dataset[WebLog] = jsonStream.select("record.*")
      .as[WebLog]

    println("webLogStream.isStreaming=" + webLogStream.isStreaming)

    val urlExtractor = """^GET (.+) HTTP/\d.\d""".r
    val allowedExtensions = Set(".html", ".htm", "")

    val contentPageLogs: String => Boolean =
      url => {
        val ext = url.takeRight(5).dropWhile(_ != '.')
        allowedExtensions.contains(ext)
      }

    val urlWebLogStream = webLogStream.flatMap { weblog =>
      weblog.request match {
        case urlExtractor(url) if contentPageLogs(url) =>
          Some(weblog.copy(request = url))
        case _ => None
      }
    }

    val rankingUrlStream = urlWebLogStream
      .groupBy($"request", window($"timestamp", "5 minutes", "1 minute"))
      .count()

    val query = rankingUrlStream.writeStream
      .queryName("urlranks")
      .outputMode(OutputMode.Complete)
      .format("memory")
      //      .option("truncate", false) // for console
      .start()


    query.awaitTermination()
  }

}