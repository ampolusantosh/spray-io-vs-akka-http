package org.anish.akka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by anish on 24/09/17.
  */
case class Permutations(input: String, permutations: List[String]) {
  // Overloaded constructor
  // This would calculate the permutations during object creation
  def this(input: String) = this(input, input.permutations.toList)
}

case class ServiceStatus(status: String, description: String)

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val permutationFormat = jsonFormat2(Permutations)
  implicit val serviceStatusFormat = jsonFormat2(ServiceStatus)
}