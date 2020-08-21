package it.unibo.intelliserra.server

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import it.unibo.intelliserra.core.actuator.{Action, Actuator, OperationalState}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor}
import it.unibo.intelliserra.device.DeviceDeploy
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.{Await}
import scala.concurrent.duration._
import it.unibo.intelliserra.common.communication._
import scala.util.{Failure, Success, Try}


@RunWith(classOf[JUnitRunner])
private class DeviceDeployTest extends TestKit(ActorSystem("MySpec"))
 with WordSpecLike
 with BeforeAndAfter
 with BeforeAndAfterAll {

  private var entityManagerActor: TestActorRef[EntityManagerActor] = _
  private implicit val timeout : Timeout = Timeout(5 seconds)
  var deviceDeploy : DeviceDeploy = DeviceDeploy()

  private val sensor:Sensor = new Sensor {
   override def identifier: String = "sensorID"

   override def capability: SensingCapability = SensingCapability(Temperature)

   override def state: Measure = ???
  }

  private val sensor2:Sensor = new Sensor {
    override def identifier: String = "sensorID"

    override def capability: SensingCapability = SensingCapability(Humidity)

    override def state: Measure = ???
  }

  private val actuator:Actuator = new Actuator {
    override def identifier: String = "actuatorID"

    override def capability: ActingCapability = ActingCapability(Set(DaiAcqua))

    override def state: OperationalState = ???

    override def doAction(action: Action): Unit = ???
  }

  private val actuator2:Actuator = new Actuator {
    override def identifier: String = "actuatorID"

    override def capability: ActingCapability = ActingCapability(Set(ApriFinestra))

    override def state: OperationalState = ???

    override def doAction(action: Action): Unit = ???
  }

  case object Temperature extends Category
  case object Humidity extends Category
  case object DaiAcqua extends Action
  case object ApriFinestra extends Action

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  before {
    entityManagerActor  = TestActorRef.create[EntityManagerActor](system , Props[EntityManagerActor])
  }

  "A deviceDeploy " must {
    "ask for a sensor assignment" in {
      val sensorProbe = TestProbe()
      deploySensor(sensor)
    }
  }

  "A deviceDeploy " must {
    "ask for a sensor assignment with an identify that already exists" in {
      val sensorProbe = TestProbe()
      deployExistingSensor(sensor2)
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment" in {
      val actuatorProbe = TestProbe()
      deployActuator(actuator)
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment with an identify that already exists" in {
      val sensorProbe = TestProbe()
      deployExistingActuator(actuator2)
    }
  }

  def deploySensor(sensor: Sensor) : Unit = {
    Try(Await.ready(deviceDeploy.deploySensor(sensor), timeout.duration)) match {
      case Success(_) => assert(true)
      case Failure(exception)=> fail(exception)
    }
  }

  def deployExistingSensor(sensor: Sensor) : Unit = {
    Try(Await.ready(deviceDeploy.deploySensor(sensor), timeout.duration)) match {
      case Success(_) => fail()
      case Failure(_)=> assert(true)
    }
  }

  def deployActuator(actuator: Actuator) : Unit = {
    Try(Await.ready(deviceDeploy.deployActuator(actuator), timeout.duration)) match {
      case Success(_) => assert(true)
      case Failure(exception)=> fail(exception)
    }
  }

  def deployExistingActuator(actuator: Actuator) : Unit = {
    Try(Await.ready(deviceDeploy.deployActuator(actuator), timeout.duration)) match {
      case Success(_) => fail()
      case Failure(_)=> assert(true)
    }
  }

}
