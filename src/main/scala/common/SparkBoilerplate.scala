package common

import org.apache.spark.sql.SparkSession

trait SparkBoilerplate {

  val spark = SparkSession.builder
    .appName(this.getClass.getSimpleName)
    /**
      * Adds new attributes to metrics:
      * - latency
      * - inputRate-total
      * - processingRate-total
      *
      *
      * Sample JSON pulled from `/metrics/json`
      * "local-1563412146036.driver.spark.streaming.urlranks.inputRate-total": {
      * "value": 4033.8134001252347
      * },
      * "local-1563412146036.driver.spark.streaming.urlranks.latency": {
      * "value": 4725
      * },
      * "local-1563412146036.driver.spark.streaming.urlranks.processingRate-total": {
      * "value": 4090.1587301587306
      * },
      * */
    .config("spark.sql.streaming.metricsEnabled", true)
    .master("local[3]")
    .getOrCreate()

}
