package functionalTests.controllers

import org.scalatest._
import play.api.test._
import org.scalatestplus.play._

abstract class BaseSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfter {
  implicit override lazy val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map(
        "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url" -> "jdbc:h2:mem:play",
        "slick.dbs.default.db.use" -> "sa",
        "slick.dbs.default.db.password" -> ""
      )
    )
}
