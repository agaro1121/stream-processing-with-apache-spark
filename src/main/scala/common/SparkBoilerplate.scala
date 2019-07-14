package common

import org.apache.spark.sql.SparkSession

trait SparkBoilerplate {

  val spark = SparkSession.builder
    .appName(this.getClass.getSimpleName)
    .master("local[3]")
    .getOrCreate()

}
