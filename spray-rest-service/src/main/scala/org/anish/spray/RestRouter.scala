package org.anish.spray

import akka.actor.{Actor, ActorSystem}
import akka.event.Logging

/**
  * Created by anish on 25/09/17.
  */
class RestRouter(implicit val system: ActorSystem) extends Actor with RestService {

  implicit val executionContext = system.dispatcher

  implicit val log = Logging(system, getClass)

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  val actorRefFactory = context

  // Here we override the receive function of the Actor to execute our Rest Route
  val receive = runRoute(route)
}
