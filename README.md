# Code Samples for `Stream Processing With Apache Spark`

# Prerequisites
```scala
val sparkVersion = "2.4.3"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core",
  "org.apache.spark" %% "spark-sql"
).map(_ % sparkVersion)
```

- Get Sample Dataset
  1. `git clone https://github.com/stream-processing-with-spark/datasets.git`
  2. `cd datasets/NASA-weblogs`
  3. `tar -xvf nasa_dataset_july_1995.tgz`
  4. `mv nasa_dataset_july_1995 /tmp`

Clean up Sample Dataset:
- `rm -rf /tmp/nasa_dataset_july_1995`


### Chapter 7
1. run tcp server that delivers logs: `sbt 'runMain chapter7.TcpServerRunner'`
2. run spark job: `chapter7.NasaDatasetStreamed`