package com.example.app

import org.scalatest._
import scala.sys.process._
import org.scalatra.test.scalatest.ScalatraFeatureSpec

class MyScalatraServletValidation extends ScalatraFeatureSpec with GivenWhenThen with BeforeAndAfterAll
{
  import MyScalatraServletValidation._

  addServlet(classOf[MyScalatraServlet], "/*")

  override def beforeAll()
  {
    super.beforeAll()
    serverUriStr = server.getURI.toString
  }

  override def afterAll()
  {
    super.afterAll()
  }

  var serverUriStr : String = null
  info("As a shipment system administrator")
  info("I would like to issue a new shipment")
  info("Then issue some tracking updates")
  info("And check the results of those updates")

  feature("Whole shipment chain tracking")
  {
    scenario("User issues a shipment and a tracking update to set it as delivered")
    {
      //Thread.sleep(1000L)
      Given("an empty shipment database")
      assert(Shipment.fetchAll().isEmpty, "The database should be empty at start up")

      When("A Shipment request is received")
      postJson(post_shipment, serverUriStr)

      Then("The shipment database has got one element")
      assert(Shipment.fetchAll().size.equals(1), "The database contains only one shipment element")
      assert(Shipment.exists(post_shipment_reference), "The regular shipment must be stored in the db")

      When("A Tracking request is received stating that the shipment has been delivered")
      val responsePut = putJson(conciliation_request_tracking, serverUriStr)
      Then("A conciliation request response is received as a response")
      assert(responsePut.contains("200 OK"))
      assert(responsePut.contains("CONCILIATION_REQUEST"))
    }
  }
}

  object MyScalatraServletValidation
  {
    /**
      * Execute the POST command passed as a String.
      * The port number must be retrieved here because it gets assigned an arbitrary integer every time the test is run.
      * @param jsonString
      * @return result of the curl command
      */
    def postJson(jsonString : String, serverUri: String) = {
      val cmd = List("curl", "-i", "-v", "-H", "Content-Type: application/json", "-d", jsonString,
            serverUri + "api/register")
      val response = cmd.!!
      println(cmd)
      println("received post response" + response)
      println("End of post response")
      response
    }

    /**
      * Execute the PUT command passed as a String.
      * The port number must be retrieved here because it gets assigned an arbitrary integer every time the test is run.
      * @param jsonString
      * @return result of the curl command
      */
    def putJson(jsonString : String, serverUri: String) = {
      val cmd = List("curl", "-i", "-v", "-H", "Content-Type: application/json", "-X PUT", "-d", jsonString,
        serverUri + "api/push")
      val response = cmd.!!
      println(cmd)
      println("received put response" + response)
      println("End of put response")
      response
    }

    // JSON double quotes must be 'escaped' so they do not get removed during the execution of the command
    // Regular POST Shipment
    val post_shipment_reference = "ABCD123456"
    val post_shipment =
      """{
        | \"reference\":\"ABCD123456\",
        | \"parcels\" : [
        | {
        |   \"weight\":1,
        |   \"width\": 10,
        |   \"height\": 10,
        |   \"length\": 10
        | },
        | {
        |   \"weight\":2,
        |   \"width\": 20,
        |   \"height\": 20,
        |   \"length\": 20
        | }]}""".stripMargin

  val conciliation_request_tracking =
    """{
      | \"status\":\"DELIVERED\",
      | \"parcels\":2,
      | \"weight\":4,
      | \"reference\":\"ABCD123456\"}""".stripMargin
}
