package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.sensor.{Measure, Sensor}
import it.unibo.intelliserra.device.core.EntityActor

class SensorActor(private val sensor: Sensor) extends EntityActor {
  override def receive: Receive = zoneManagement orElse fallback
}

object SensorActor{

  private case class SensorStateChange(state: Measure)

  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new SensorActor(sensor)), name = sensor.identifier)
  }
}
