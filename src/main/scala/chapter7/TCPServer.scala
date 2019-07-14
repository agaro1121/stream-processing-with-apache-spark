package chapter7

import java.io.File
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

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

    val files = getListOfFiles("/Users/hierro/git/datasets/NASA-weblogs/nasa_dataset_july_1995")
        .filter(_.endsWith(".json"))

    println(s"files=${files.mkString("\n")}")

    val flow = Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      // Required because spark does not send an initial message
      // This sends a fake message to the server to initiate the stream of text
      .merge(Source.single("{}"))
      .flatMapConcat { message =>
        println(s"Received message=$message")
        Source(files)
          .flatMapConcat(file => FileIO.fromPath(Paths.get(file)))
      }
      .throttle(40, 500 millis)
//      .take(40) // in case you want to short-circuit your stream

    connections.runForeach { connection =>
      println(s"New connection from: ${connection.remoteAddress}")
      connection.handleWith(flow)
    }
  }
}

object TcpServerRunner extends App {
  TCPServer.start()
}