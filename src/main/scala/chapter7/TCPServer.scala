package chapter7

import java.io.File
import java.nio.file.Paths
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Source, Tcp}
import akka.util.ByteString
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import common.DatasetHelpers

object TCPServer {
  def start() = {

    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val mat: Materializer = ActorMaterializer()

    val connections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
      Tcp().bind("localhost", 9999)

    def getListOfFiles(dir: String): List[String] = {
      val d = new File(dir)
      if (d.exists && d.isDirectory) {
        d.listFiles.filter(_.isFile).toList.map(_.getAbsolutePath)
      } else {
        List[String]()
      }
    }

    val files = getListOfFiles(DatasetHelpers.datasetDirectory)
        .filter(_.endsWith(".json"))

    println(s"files=${files.mkString("\n")}")

    val flow = Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      /* Required because the Spark socket client does not send an initial message
         This sends a fake message to itself to initiate the stream of text
       */
      .merge(Source.single("{}"))
      .flatMapConcat { message =>
        println(s"Received message=$message")
        Source(files)
          .flatMapConcat(file => FileIO.fromPath(Paths.get(file)))
      }
      .throttle(40, 500 millis)
    // uncomment below in case you want to end your stream early
    //.take(40)

    connections.runForeach { connection =>
      println(s"New connection from: ${connection.remoteAddress}")
      connection.handleWith(flow)
    }
  }
}

object TcpServerRunner extends App {
  TCPServer.start()
}