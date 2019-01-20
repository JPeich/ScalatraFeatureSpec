package com.example.app

import org.scalatra.test.scalatest._

import org.json4s._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

class MyScalatraServletTests extends ScalatraFunSuite
{
  addServlet(classOf[MyScalatraServlet], "/*")
  import MyScalatraServletTests._

  test("GET / on MyScalatraServlet should return status 200") {
    get("/") {
      status should equal (200) // OK
    }
  }

  test("POST /shipment for a regular JSON Shipment should return status 201") {
    postJson("/api/register", post_shipment) {
      status should equal (201) // Created
    }
  }

  test("POST /shipment for a regular JSON Shipment must be stored in the database") {
    postJson("/api/register", post_shipment) {
      // Check that the shipment has been stored in the database
      val referenceFromAnswer = header("Location").split("/").last
      assert(Shipment.exists(referenceFromAnswer), "The regular shipment must be stored in the db")
    }
  }

  test("POST /shipment for a wrong JSON Shipment should return status 201") {
    postJson("/api/register", wrong_post_shipment) {
      status should equal (400) // BadRequest
      body contains("Some of the fields for the shipment request are empty.")
    }
  }

  test("PUT /tracking for a delivered shipment must have the CONCILIATION_REQUEST status") {
    postJson("/api/register", post_shipment) {
      putJson("/api/push", conciliation_request_tracking) {
        status should equal (200) // OK
        val jsonBody: JValue = JsonMethods.parse(body)
        jsonBody \ "status" should equal(JString("CONCILIATION_REQUEST"))
      }
    }
  }

  test("PUT /tracking for a not needed shipment must have the NOT_NEEDED status") {
    postJson("/api/register", post_shipment) {
      putJson("/api/push", not_needed_tracking) {
        status should equal (200) // OK
        val jsonBody: JValue = JsonMethods.parse(body)
        jsonBody \ "status" should equal(JString("NOT_NEEDED"))
      }
    }
  }

  test("PUT /tracking for an unknown shipment must have the NOT_FOUND status") {
    putJson("/api/push", not_found_tracking) {
      status should equal (200) // OK
      val jsonBody: JValue = JsonMethods.parse(body)
      jsonBody \ "status" should equal(JString("NOT_FOUND"))
    }
  }

  test("PUT /tracking for some null values should return the INCOMPLETE status") {
    postJson("/api/register", post_shipment) {
      putJson("/api/push", wrong_request_tracking) {
        status should equal (200) // OK
        val jsonBody: JValue = JsonMethods.parse(body)
        jsonBody \ "status" should equal(JString("INCOMPLETE"))
      }
    }
  }

  test("PUT /tracking for a waiting in hub shipment must have the INCOMPLETE status") {
    postJson("/api/register", post_shipment) {
      putJson("/api/push", incomplete_tracking) {
        status should equal (200) // OK
        val jsonBody: JValue = JsonMethods.parse(body)
        jsonBody \ "status" should equal(JString("INCOMPLETE"))
      }
    }
  }
  def postJson[A](uri: String, body: String, headers: Map[String, String] = Map.empty[String, String])(f: => A): A =
    post(uri, body.getBytes("utf-8"), Map("Content-Type" -> "application/json") ++ headers)(f)

  def putJson[A](uri: String, body: String, headers: Map[String, String] = Map.empty[String, String])(f: => A): A =
    put(uri, body.getBytes("utf-8"), Map("Content-Type" -> "application/json") ++ headers)(f)
}


object MyScalatraServletTests
{
  // Regular POST Shipment
  val post_shipment =
    """{
      | "reference":"ABCD123456",
      | "parcels" : [
      | {
      |   "weight":1,
      |   "width": 10,
      |   "height": 10,
      |   "length": 10
      | },
      | {
      |   "weight":2,
      |   "width": 20,
      |   "height": 20,
      |   "length": 20
      | }]
      | }""".stripMargin

  // POST Shipment with typo in the "length" field
  val wrong_post_shipment =
    """{
      | "reference":"ABCD123456",
      | "parcels" : [
      | {
      |   "weight":1,
      |   "width": 10,
      |   "height": 10,
      |   "lenght": 10
      | }]}""".stripMargin

  val conciliation_request_tracking =
    """{
      |"status":"DELIVERED",
      |"parcels":2,
      |"weight":4,
      |"reference":"ABCD123456"}""".stripMargin

  val not_needed_tracking =
    """{
      |"status":"DELIVERED",
      |"parcels":2,
      |"weight":3,
      |"reference":"ABCD123456"}""".stripMargin

  val not_found_tracking =
    """{
      |"status":"DELIVERED",
      |"parcels":2,
      |"weight":4,
      |"reference":"ABCD"}""".stripMargin

  val wrong_request_tracking =
    """{
      |"status":"DELIVERED",
      |"parcels":null,
      |"weight":4,
      |"reference":"ABCD123456"}""".stripMargin

  val incomplete_tracking =
    """{
      |"status":"WAITING_IN_HUB",
      |"parcels":2,
      |"weight":4,
      |"reference":"ABCD123456"}""".stripMargin
}
