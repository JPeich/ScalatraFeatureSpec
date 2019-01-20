package com.example.app

import org.json4s._

case class Tracking(status: Option[String], parcels: Option[Int], weight: Option[Int], reference: Option[String])

object Tracking
{
  val result_not_found = "NOT_FOUND"
  val result_incomplete = "INCOMPLETE"
  val result_not_needed = "NOT_NEEDED"
  val result_conciliation_request = "CONCILIATION_REQUEST"
  val DELIVERED = "DELIVERED"

  // Check whether the reference field is empty
  def isReferenceEmpty(tracking: Tracking): Boolean =
    tracking.reference.isEmpty

  // Check whether any of the fields in a Shipment has got None assigned. The reference field is not taken into account.
  def isWrongTracking(tracking: Tracking): Boolean =
    tracking.status.isEmpty || tracking.parcels.isEmpty || tracking.weight.isEmpty

  def processTracking(tracking: Tracking): JValue =
  {
    println(s"Received tracking: $tracking")
    val status : String = processStatus(tracking)
    // Return a JValue from the reference and the status
    JObject("reference" -> JString(tracking.reference.get), "status" -> JString(status))
  }

  // Process the tracking message and return either "CONCILIATION_REQUEST", "NOT_NEEDED", "INCOMPLETE" or "NOT_FOUND"
  def processStatus(tracking: Tracking): String =
  {
    // Check if the Shipment exists in the database
    if(!Shipment.exists(tracking.reference.get))
      {
        result_not_found
      }
    // Check whether any field is null or the status is not equal to DELIVERED
    else if(isWrongTracking(tracking) || tracking.status.get != DELIVERED)
      {
        result_incomplete
      }
    // DELIVERED tracking
    else
    {
      // Get the shipment's parcels
      val parcels : List[Parcel] = Shipment.getParcels(tracking.reference.get).getOrElse(List.empty[Parcel])
      // Calculate total weight of shipment and compare it with the tracking weight
      val totalShipmentWeight : Int = parcels.foldLeft(0) { (acc, p :Parcel) => acc + p.weight.get }

      if(totalShipmentWeight < tracking.weight.get)
        {
          result_conciliation_request
        }
      else
        {
          result_not_needed
        }
    }
  }
}
