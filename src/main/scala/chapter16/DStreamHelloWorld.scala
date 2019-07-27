package chapter16

import scala.io.StdIn

import common.SparkBoilerplate
import org.apache.spark.streaming._

object DStreamHelloWorld extends SparkBoilerplate with App {

  val scc = new StreamingContext(spark.sparkContext, Duration(2000L))

  val dstream = scc.socketTextStream("localhost", 9999)

  val countStream = dstream.count

  countStream.print

  scc.start()

  StdIn.readLine

}