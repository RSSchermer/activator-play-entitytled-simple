package controllerTests

import org.scalatest._
import play.api.test._
import org.scalatestplus.play._

abstract class BaseSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfter {
  implicit override lazy val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map(
        "db.default.driver" -> "org.h2.Driver",
        "db.default.url" -> "jdbc:h2:mem:play",
        "db.default.user" -> "sa",
        "db.default.password" -> ""
      )
    )
}
