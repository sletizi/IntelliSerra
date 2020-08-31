package it.unibo.intelliserra.client.core

import scala.concurrent.Future

trait ZoneClient {
  type Zone = String
  type Entity = String

  /**
   * Create a new zone with specified name
   * @param zone  the name of zone
   * @return if success, the name of zone, otherwise a failure
   */
  def createZone(zone: Zone): Future[Zone]


  /**
   * Remove an existing zone with specified name
   * @param zone  the name of zone
   * @return if success, the name of zone, otherwise a failure
   */
  def removeZone(zone: Zone): Future[Zone]

  /**
   * Obtain all existing zones
   * @return if success, a list of alla existing zones, otherwise a failure
   */
  def zones(): Future[List[Zone]]

  /**
  * Associate the specified entity (both actuators and sensors) to the specified zone, if possible
  * @param entity, name of the entity
    * @param zone, name of the zone
    * @return if success, the name of the entity, a failure otherwise
  */
  def associateEntity(entity: Entity, zone: Zone): Future[Zone]
}