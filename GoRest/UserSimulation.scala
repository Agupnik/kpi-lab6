package GoRest

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object RandomGenerator {
  def randomString(length: Int): String = {
    val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val salt = new StringBuilder
    val rnd = new scala.util.Random
    while (salt.length < length) { // length of the random string.
      val index = (rnd.nextFloat() * SALTCHARS.length).asInstanceOf[Int]
      salt.append(SALTCHARS.charAt(index))
    }
    val saltStr = salt.toString
    saltStr
  }

  def randomEmail(): String = randomString(10) + "@gmail.com"

  def randomUserRequest() : String = """{"name":"""".stripMargin + RandomGenerator.randomString(25) + """",
                                   |"gender":"male",
                                   |"email":"""".stripMargin + RandomGenerator.randomEmail() + """",
                                   | "status":"active"} """.stripMargin
}

class UserSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://gorest.co.in")
    .authorizationHeader("Bearer ee3009c0b3cd899cb3c1be0ee6a9fcae37f53e6247c0fc07457ca20ccd964c83")


  val post = scenario("Post User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
    )

  val get = scenario("Get User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(
      http("Get User")
        .get("/public/v1/users/${userId}")
    )

  val put = scenario("Put User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomUserRequest())
      sessionPutUpdate
    })
    .exec(
      http("Put User")
        .put("/public/v1/users/${userId}")
        .body(StringBody("${putrequest}")).asJson
    )

  val delete = scenario("Delete User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomUserRequest())
      sessionPutUpdate
    })
    .exec(
      http("Delete User")
        .delete("/public/v1/users/${userId}")
    )

  setUp(post.inject(rampUsers(30).during(25.seconds)).protocols(httpProtocol),
    get.inject(rampUsers(30).during(25.seconds)).protocols(httpProtocol),
    put.inject(rampUsers(30).during(25.seconds)).protocols(httpProtocol),
    delete.inject(rampUsers(30).during(25.seconds)).protocols(httpProtocol))
}
