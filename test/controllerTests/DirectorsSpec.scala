package controllerTests

import org.scalatest._
import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.db.slick.DB

import scala.slick.jdbc.{ StaticQuery => Q }

import models.meta.Profile.driver.simple._
import models._
import controllers._

class DirectorsSpec extends BaseSpec {
  before {
    DB.withTransaction { implicit session =>
      Director.insert(Director(None, "Alfred Hitchcock"))
      Director.insert(Director(None, "Steven Spielberg"))
    }
  }

  after {
    DB.withSession { implicit session =>
      // Delete instead of truncate because of foreign key constraint
      Q.updateNA("delete from DIRECTORS").execute
    }
  }

  "Directors#list" must {
    "display all directors" in {
      val result = Directors.list().apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Alfred Hitchcock")
      bodyText must include ("Steven Spielberg")
    }
  }

  "Directors#show" must {
    "present the correct director" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        val result = Directors.show(hitchcockID.value).apply(FakeRequest())
        val bodyText = contentAsString(result)

        bodyText must include ("Alfred Hitchcock")
      }
    }
  }

  "Directors#save with valid data" must {
    val request = FakeRequest().withFormUrlEncodedBody(
      "id" -> "",
      "name" -> "Martin Scorcese"
    )

    "create a new record" in {
      await(Directors.save().apply(request))

      DB.withSession { implicit session =>
        Director.list.length mustBe 3
      }
    }

    "redirect to the directors list" in {
      val response = Directors.save().apply(request)
      val header = await(response).header

      header.status mustBe 303
      header.headers.get("location").get mustBe routes.Directors.list().url
    }
  }

  "Directors#save without a name" must {
    val request = FakeRequest().withFormUrlEncodedBody(
      "id" -> "",
      "name" -> ""
    )

    "must return a 400 respone" in {
      val response = await(Directors.save().apply(request))

      response.header.status mustBe 400
    }
  }

  "Directors#edit" must {
    "present the correct director" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        val result = Directors.edit(hitchcockID.value).apply(FakeRequest())
        val bodyText = contentAsString(result)

        bodyText must include ("Alfred Hitchcock")
      }
    }
  }

  "Directors#update with valid data" must {
    "update the record" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        val request = FakeRequest().withFormUrlEncodedBody(
          "id" -> hitchcockID.value.toString,
          "name" -> "Alfed H."
        )

        await(Directors.update(hitchcockID.value).apply(request))

        Director.find(hitchcockID).get.name mustBe "Alfed H."
      }
    }

    "redirect to the director's show page" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        val request = FakeRequest().withFormUrlEncodedBody(
          "id" -> hitchcockID.value.toString,
          "name" -> "Alfed H."
        )

        val response = Directors.update(hitchcockID.value).apply(request)
        val header = await(response).header

        header.status mustBe 303
        header.headers.get("location").get mustBe routes.Directors.show(hitchcockID).url
      }
    }
  }

  "Directors#update without a name" must {
    "return a 400 response" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        val request = FakeRequest().withFormUrlEncodedBody(
          "id" -> hitchcockID.value.toString,
          "name" -> ""
        )

        val response = await(Directors.update(hitchcockID.value).apply(request))

        response.header.status mustBe 400
      }
    }
  }

  "Directors#remove" must {
    "present the correct director" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        val result = Directors.remove(hitchcockID.value).apply(FakeRequest())
        val bodyText = contentAsString(result)

        bodyText must include ("Alfred Hitchcock")
      }
    }
  }

  "Directors#delete" must {
    "delete 1 director" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        await(Directors.delete(hitchcockID.value).apply(FakeRequest()))

        Director.list.length mustBe 1
      }
    }

    "delete the correct director" in {
      DB.withSession { implicit session =>
        val hitchcockID = Director.query.filter(_.name === "Alfred Hitchcock").map(_.id).firstOption.get

        await(Directors.delete(hitchcockID.value).apply(FakeRequest()))

        Director.find(hitchcockID) mustBe None
      }
    }
  }
}
