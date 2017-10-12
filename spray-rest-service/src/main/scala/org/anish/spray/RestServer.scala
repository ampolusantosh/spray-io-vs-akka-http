package org.anish.spray

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.util.Try

/**
  * Created by anish on 09/06/17.
  */
object RestServer extends App {

  implicit val system = ActorSystem("spray-rest-server")

  val config = ConfigFactory.load()

  val host = Try(config.getString("http.interface")).getOrElse("0.0.0.0")
  val port = if (sys.env.contains("PORT")) sys.env("PORT").toInt else Try(config.getInt("http.port")).getOrElse(9090)

  // the gateway actor replies to incoming HttpRequests
  val restServiceActor = system.actorOf(Props(new RestRouter), name = "PermutationsRouter")

  try {
    IO(Http) ! Http.Bind(restServiceActor, host, port)
  } catch {
    case e: Exception =>
      println("Error binding : {}", e.getMessage)
      restServiceActor ! PoisonPill
      system.shutdown()
  }

}