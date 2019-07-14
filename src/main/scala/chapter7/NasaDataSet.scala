package chapter7

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import java.sql.Timestamp
import common.SparkBoilerplate

final case class WebLog(host: String, timestamp: Timestamp, request: String, http_reply: Int, bytes: Long)

object NasaDataSet extends SparkBoilerplate {
  import spark.implicits._

  def main(args: Array[String]): Unit = {

    val logsDirectory = "/Users/hierro/git/datasets/NASA-weblogs/nasa_dataset_july_1995"
    val rawLogs = spark.read.json(logsDirectory)

    val preparedLogs = rawLogs.withColumn("http_reply", $"http_reply".cast(IntegerType))
    val weblogs = preparedLogs.as[WebLog]

    val recordCount = weblogs.count
    println(recordCount)

    val topDailyURLS = weblogs.
      withColumn("dayOfMonth", dayofmonth($"timestamp")).
      select($"request", $"dayOfMonth").
      groupBy($"dayOfMonth", $"request").
      agg(count($"request").alias("count")).
      orderBy(desc("count"))

    val urlExtractor = """^GET (.+) HTTP/\d.\d""".r
    val allowedExtensions = Set(".html", ".htm", "")

    val contentPages = weblogs.filter{ log =>
      log.request match {
        case urlExtractor(url) =>
          val ext = url.takeRight(5).dropWhile(_ != '.')
          allowedExtensions.contains(ext)
        case _ => false
      }
    }

    val topContentPages = contentPages.
      withColumn("dayOfMonth", dayofmonth($"timestamp")).
      select($"request", $"dayOfMonth").
      groupBy($"dayOfMonth", $"request").
      agg(count($"request").alias("count")).
      orderBy(desc("count"))

      topContentPages.show(truncate = false)

  }
}
