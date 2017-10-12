package org.anish.akka

import akka.event.LoggingAdapter
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by anish on 24/09/17.
  */
trait RestService extends JsonSupport {

  implicit val log: LoggingAdapter

  implicit val executionContext: ExecutionContext

  val route =
    logRequestResult("akka-http-service") {
      pathSingleSlash {
        get {
          complete {
            ServiceStatus("OK", "Welcome to Akka Http Rest Service")
          }
        }
      } ~ pathPrefix("sync") {
        pathPrefix("permutations") {
          get {
            parameters('string.as[String]) { (inputString) =>
              complete {
                new Permutations(inputString)
              }
            }
          }

        }
      } ~ pathPrefix("async") {
        pathPrefix("permutations") {
          get {
            parameters('string.as[String]) { (inputString) =>

              val respFut = Future {
                log.info("Processing request for {} in Future", inputString)
                new Permutations(inputString)
              }

              onComplete(respFut) {
                case Success(successPermutations: Permutations) => complete(successPermutations)
                case Failure(ex) =>
                  log.error("Exception while processing : {}", ex.getMessage)
                  complete(StatusCodes.InternalServerError, ServiceStatus("ERROR", "Some error in service"))
              }
            }
          }

        }
      }
    }
}
