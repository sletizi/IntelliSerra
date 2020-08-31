package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.communication.{Messages, Protocol}
import it.unibo.intelliserra.common.communication.Protocol._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * This is the Actor Controller who is in charge of receiving client messages and sending them to other entities based on the request.
 * He also knows the references to the main entities such as zoneManagerActor and entityManagerActor.
 *
 * @param zoneManagerActor, actorRef
 * @param entityManagerActor, actorRef
 */
//noinspection ScalaStyle
private[server] class GreenHouseController(zoneManagerActor: ActorRef, entityManagerActor: ActorRef) extends Actor with ActorLogging {
  type ResponseMap[T] = PartialFunction[Try[T], ServiceResponse]

  private implicit val system: ActorSystem = context.system
  private implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val ec: ExecutionContext = context.dispatcher

  private def sendResponseWithFallback[T](request: Future[T], replyTo: ActorRef)(mapSend: ResponseMap[T]): Unit = {
    request onComplete {
      sendResponseWithFallback(replyTo)(mapSend)
    }
  }

  private def sendResponse[T](replyTo: ActorRef)(mapSend: ResponseMap[T]): Try[T] => Unit = replyTo ! mapSend(_)

  private def sendResponseWithFallback[T](replyTo: ActorRef)(mapSend: ResponseMap[T]): Try[T] => Unit = sendResponse(replyTo)(mapSend orElse fallback)

  private def fallback[T]: ResponseMap[T] = {
    case Failure(exception) => ServiceResponse(Error, exception.toString)
    case _ => ServiceResponse(Error, "Internal Error")
  }

  /* --- ON RECEIVE ACTIONS --- */
  override def receive: Receive = {

    case CreateZone(zoneName) =>
      sendResponseWithFallback(zoneManagerActor ? Messages.CreateZone(zoneName), sender()) {
        case Success(Messages.ZoneCreated) => ServiceResponse(Created)
        case Success(Messages.ZoneAlreadyExists) => ServiceResponse(Conflict)
      }

    case DeleteZone(zoneName) =>
      sendResponseWithFallback(zoneManagerActor ? Messages.RemoveZone(zoneName), sender()) {
        case Success(Messages.ZoneRemoved) => ServiceResponse(Deleted)
        case Success(Messages.ZoneNotFound) => ServiceResponse(NotFound, "Zone not found")
      }

    case GetZones() =>
      sendResponseWithFallback(zoneManagerActor ? Messages.GetZones, sender()) {
        case Success(Messages.ZonesResult(zones)) => ServiceResponse(Ok, zones)
      }

    case AssignEntity(zoneName, entityId) =>
      val association =
        entityManagerActor ? Messages.GetEntity(entityId) flatMap {
          case Messages.EntityResult(entity) =>
            zoneManagerActor ? Messages.AssignEntityToZone(zoneName, entity)
          case msg => Future.successful(msg)
        }
      sendResponseWithFallback(association, sender()) {
        case Success(Messages.EntityNotFound) => ServiceResponse(NotFound, "Entity not found")
        case Success(Messages.ZoneNotFound) => ServiceResponse(NotFound, "Zone not found")
        case Success(Messages.AlreadyAssigned(zone)) => ServiceResponse(Conflict, zone)
        case Success(Messages.AssignOk) => ServiceResponse(Ok)
        case Success(Messages.AssignError(error)) => ServiceResponse(Error, error)
      }

    case DissociateEntity(entityId) =>
      val association =
        entityManagerActor ? Messages.GetEntity(entityId) flatMap {
          case Messages.EntityResult(entity) =>
            zoneManagerActor ? Messages.DissociateEntityFromZone(entity)
          case msg => Future.successful(msg)
        }
      sendResponseWithFallback(association, sender()) {
        case Success(Messages.DissociateOk) => ServiceResponse(Ok)
        case Success(Messages.AlreadyDissociated) => ServiceResponse(Error)
        case Success(Messages.EntityNotFound) => ServiceResponse(NotFound, "Entity not found")
      }

    case Protocol.RemoveEntity(entityId) =>
      sendResponseWithFallback(entityManagerActor ? Messages.RemoveEntity(entityId), sender()) {
        case Success(Messages.EntityNotFound) => ServiceResponse(NotFound, "Entity not found")
        case Success(Messages.EntityRemoved) => ServiceResponse(Deleted)
      }
  }
}

object GreenHouseController {
  val name = "GreenHouseController"

  def apply(zoneManagerActor: ActorRef, entityManagerActor: ActorRef)(implicit actorSystem: ActorSystem): ActorRef =
    actorSystem actorOf(Props(new GreenHouseController(zoneManagerActor, entityManagerActor)), name)
}

