name := "stream-processing-with-spark"

version := "0.1"

scalaVersion := "2.12.8"

val sparkVersion = "2.4.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core",
  "org.apache.spark" %% "spark-sql"
).map(_ % sparkVersion)