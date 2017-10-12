package org.anish.akka

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.Try

/**
  * Created by anish on 09/06/17.
  */
object  RestServer extends App
  with RestService {

  implicit val system = ActorSystem("akka-http-rest-server")
  implicit val materializer = ActorMaterializer()
  override implicit val executionContext: ExecutionContext = system.dispatcher
  override implicit val log: LoggingAdapter = Logging(system, getClass)

  val config = ConfigFactory.load()

  val host = Try(config.getString("http.interface")).getOrElse("0.0.0.0")
  val port = if (sys.env.contains("PORT")) sys.env("PORT").toInt else Try(config.getInt("http.port")).getOrElse(9090)

  val bindingFuture = Http().bindAndHandle(route, host, port)

  println(s"Server online at http://$host:$port\nPress Return to Stop")

  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => {
    println("Gracefully Terminating Server")
    system.terminate()
  }) // and shutdown when done

}