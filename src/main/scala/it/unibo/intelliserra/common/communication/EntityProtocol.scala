package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}

trait EntityProtocol {

  sealed trait JoinRequest
  case class JoinSensor(identifier: String, sensingCapability: SensingCapability, sensorRef: ActorRef) extends JoinRequest
  case class JoinActuator(identifier: String, actingCapability: ActingCapability, actuatorRef : ActorRef) extends JoinRequest

  sealed trait JoinResult
  case object JoinOK extends JoinResult
  case class JoinError(error:String) extends JoinResult
}
