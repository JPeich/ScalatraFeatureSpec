package com.example.app

import scala.collection.concurrent.TrieMap

case class Shipment(reference: Option[String], parcels: List[Parcel])

object Shipment
{
  private val db = TrieMap.empty[String, Shipment]

  // Check whether any of the fields in a Shipment has got None assigned
  def isAnyFieldNone(shipment :Shipment): Boolean =
    shipment.reference.isEmpty || shipment.parcels.exists(Parcel.isAnyFieldNone)

  def find(reference: String): Option[Shipment] = db.get(reference)

  def fetchAll(): List[Shipment] = db.values.toList

  def exists(reference: String): Boolean = db.contains(reference)

  def save(shipment: Shipment): Option[Shipment] = db.put(shipment.reference.get, shipment)

  def getParcels(reference: String): Option[List[Parcel]] = find(reference) match {
    case Some(shipment) => Some(shipment.parcels)
    case None => None
  }
}