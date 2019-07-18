package chapter7

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import common.DatasetHelpers.WebLog
import common.{DatasetHelpers, SparkBoilerplate}

object NasaDataSet extends SparkBoilerplate {
  import spark.implicits._

  def main(args: Array[String]): Unit = {

    val rawLogs = spark.read.json(DatasetHelpers.datasetDirectory)

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
