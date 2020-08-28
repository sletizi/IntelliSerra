package it.unibo.intelliserra.device

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateToMe}
import it.unibo.intelliserra.core.entity.SensingCapability
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor}
import it.unibo.intelliserra.device.core.sensor.SensorActor
import it.unibo.intelliserra.utils.TestUtility
import monix.reactive.Observable
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SensorActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var sensorActor: TestActorRef[SensorActor] = _

  private val mockSensor = new Sensor {
    case object Temperature extends Category
    override def identifier: String = ""
    override def capability: SensingCapability = SensingCapability(Temperature)
    override def measures: Observable[Measure] = Observable()
  }

  before {
    sensorActor = TestActorRef.create(system, Props(new SensorActor(mockSensor)))
  }

  after {
    killActors(sensorActor)
  }

  "A sensor actor " must {
    "send an ack when is associated" in {
      val zoneManagerProbe = TestProbe()
      sensorActor ! AssociateToMe(zoneManagerProbe.ref)
      zoneManagerProbe.expectMsg(Ack)
    }
  }

}
