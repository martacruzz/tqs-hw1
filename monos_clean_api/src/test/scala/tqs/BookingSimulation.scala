package tqs

import scala.language.postfixOps

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BookingSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // feeder of municipalities
  val municipalityFeeder = Iterator.continually(
    Map("municipality" -> List("LISBOA", "PORTO", "BRAGA", "FARO", "COIMBRA").lift(scala.util.Random.nextInt(5)).get)
  )

  // scenario -- citizen creates a booking and checks status
  val citizenFlow = scenario("CitizenBookingFlow")
    .feed(municipalityFeeder)
    .exec(
      http("Create Booking")
        .post("/api/bookings")
        .body(StringBody(
          """{
            |  "municipality": "${municipality}",
            |  "description": "Old sofa and mattress",
            |  "collectionDate": "2025-11-10",
            |  "timeSlot": "MORNING",
            |  "contactInfo": "user@example.com",
            |  "address": "Main Str."
            |}""".stripMargin))
        .check(status.is(201))
        .check(jsonPath("$.token").saveAs("bookingToken")) // save booking token
    )
    .pause(1 seconds) // simulate user reading success message
    .exec(
      http("Check Booking Status")
        .get("/api/bookings/${bookingToken}")
        .check(status.is(200))
    )

  // scenario -- staff updates booking status
  val staffUpdate = scenario("StaffUpdateFlow")
    .feed(municipalityFeeder)
    .exec(
      http("Create Booking for Staff Test")
        .post("/api/bookings")
        .body(StringBody(
          """{
            |  "municipality": "${municipality}",
            |  "description": "Refrigerator",
            |  "collectionDate": "2025-11-11",
            |  "timeSlot": "AFTERNOON",
            |  "contactInfo": "stafftest@example.com",
            |  "address": "Av. da Liberdade 456"
            |}""".stripMargin))
        .check(status.is(201))
        .check(jsonPath("$.token").saveAs("staffToken")) // save token
    )
    .pause(2 seconds)
    .exec(
      http("Update to ASSIGNED")
        .patch("/api/staff/bookings/${staffToken}/update") // send api request
        .queryParam("newStatus", "ASSIGNED") // update status to assigned
        .check(status.is(200))
    )
    .pause(1 seconds)
    .exec(
      http("Update to IN_PROGRESS")
        .patch("/api/staff/bookings/${staffToken}/update")
        .queryParam("newStatus", "IN_PROGRESS")
        .check(status.is(200))
    )

  // scenario -- test capacity limit (15 bookings per slot)
  val capacityTest = scenario("CapacityLimitTest")
    .repeat(16, "i") { // try to create 16 bookings -- last one should fail
      exec(
        http("Create Booking ${i}")
          .post("/api/bookings")
          .body(StringBody(
            """{
              |  "municipality": "LISBOA",
              |  "description": "Item ${i}",
              |  "collectionDate": "2025-12-12",
              |  "timeSlot": "EVENING",
              |  "contactInfo": "capacity@example.com",
              |  "address": "Capacity Test St"
              |}""".stripMargin))
          .check(status.in(201, 400)) // 201 = success, 400 = capacity exceeded
      )
      .pause(0.5 seconds)
    }

  // inject scenarios
  setUp(
    citizenFlow.inject(
      // 30 citizens for 30 seconds
      rampUsers(30) during (30.seconds)
    ),
    staffUpdate.inject(
      // 5 staff
      constantUsersPerSec(1) during (10.seconds)
    ),
    capacityTest.inject(
      // run capacity test once
      atOnceUsers(1)
    )
  ).protocols(httpProtocol)
}