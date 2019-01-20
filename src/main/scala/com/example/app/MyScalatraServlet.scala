package com.example.app

import org.scalatra._
import org.scalatra.json._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{parse, compact}
import org.json4s.jackson.JsonMethods.parse

class MyScalatraServlet extends ScalatraServlet with JacksonJsonSupport {

  implicit lazy val jsonFormats = DefaultFormats

  post("/api/register")
  {
    def parseShipment(jv: JValue): Shipment =
    {
      val reference = (jv \ "reference").extractOpt[String]
      val parcelsList = (jv \ "parcels").children.map(_.extract[Parcel])
      Shipment(reference, parcelsList)
    }

    // Parse shipment from json body
    val shipment = parseShipment(parsedBody)
    println(s"Received shipment: $shipment")
    // Check whether any of the fields is not present
    if (Shipment.isAnyFieldNone(shipment))
      {
        BadRequest("Some of the fields for the shipment request are empty.")
      }
    else
      {
        // Store the shipment in the internal database. Shipments with the same reference will be overwritten.
        Shipment.save(shipment)
        // Return the created path for this shipment to the web service user
        val location = s"/api/register/${shipment.reference.get}"
        Created(shipment, headers = Map("Location" -> location))
      }
  }

  put("/api/push")
  {
    def parseTracking(jv: JValue): Tracking =
    {
      val status = (jv \ "status").extractOpt[String]
      val parcels = (jv \ "parcels").extractOpt[Int]
      val weight = (jv \ "weight").extractOpt[Int]
      val reference = (jv \ "reference").extractOpt[String]
      Tracking(status, parcels, weight, reference)
    }

    // Parse tracking from json body
    val tracking = parseTracking(parsedBody)
    // Check whether any of the fields is not present
    if (Tracking.isReferenceEmpty(tracking))
    {
      BadRequest("The reference field of the tracking is empty.")
    }
    else
    {
      // process the tracking and return a JValue with the response
      val result = Tracking.processTracking(tracking)
      println(compact(result))
      result
    }
  }

  get("/")
  {
    views.html.hello()
  }

}
