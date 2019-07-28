package chapter20

import common.SparkBoilerplate
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{ConstantInputDStream, InputDStream}
import org.apache.spark.streaming.{Duration, StreamingContext}
import scala.collection.mutable
import scala.io.StdIn

/**
  * File Source requires HDFS-compatible directory
  * so can't create one locally....for now
  * */


/**
  * Can be used add events during unit tests
  * on the fly
  * */
object QueueSource extends SparkBoilerplate with App {

  val scc = new StreamingContext(spark.sparkContext, Duration(2000L))

  val queue: mutable.Queue[RDD[String]] =
    new mutable.Queue[RDD[String]]()

  val source: InputDStream[String] =
    scc.queueStream(queue, true)

  source.print()

  val data = List(
    "Bruce, Wayne, Batman",
    "Clark, Kent, Superman"
  )

  val rdds = data.map(sh => spark.sparkContext.parallelize(Seq(sh)))

  queue.enqueue(rdds:_*)

  scc.start()

  StdIn.readLine

}

/**
  * Repeats entire RDD.
  * Useful as a Data Generator while testing
  * */
object ConstantInputDStreamSource extends SparkBoilerplate with App {

  val scc = new StreamingContext(spark.sparkContext, Duration(2000L))

  val data = List(
    "Bruce, Wayne, Batman",
    "Clark, Kent, Superman"
  )
  val rdd = spark.sparkContext.parallelize(data)

  val source: InputDStream[String] =
    new ConstantInputDStream(scc, rdd)

  source.print()

  scc.start()

  StdIn.readLine

}

