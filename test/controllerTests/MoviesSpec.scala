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

class MoviesSpec extends BaseSpec {
  before {
    DB.withTransaction { implicit session =>
      val hithcockID = Director.insert(Director(None, "Alfred Hitchcock"))
      val cameronID = Director.insert(Director(None, "James Cameron"))
      
      Movie.insert(Movie(None, "Titanic", Some(cameronID)))
      Movie.insert(Movie(None, "Vertigo", Some(hithcockID)))
    }
  }

  after {
    DB.withSession { implicit session =>
      // Delete instead of truncate because of foreign key constraint
      Q.updateNA("delete from DIRECTORS").execute
      Q.updateNA("delete from MOVIES").execute
    }
  }

  "Movies#list" must {
    "display all movies" in {
      val result = Movies.list().apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Titanic")
      bodyText must include ("Vertigo")
    }
  }

  "Movies#show" must {
    "present the correct movie" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        val result = Movies.show(titanicID.value).apply(FakeRequest())
        val bodyText = contentAsString(result)

        bodyText must include ("Titanic")
      }
    }
  }

  "Movies#save with valid data" must {
    val request = FakeRequest().withFormUrlEncodedBody(
      "id" -> "",
      "title" -> "E.T."
    )

    "create a new record" in {
      await(Movies.save().apply(request))

      DB.withSession { implicit session =>
        Movie.list.length mustBe 3
      }
    }

    "redirect to the movies list" in {
      val response = Movies.save().apply(request)
      val header = await(response).header

      header.status mustBe 303
      header.headers.get("location").get mustBe routes.Movies.list().url
    }
  }

  "Movies#save without a title" must {
    val request = FakeRequest().withFormUrlEncodedBody(
      "id" -> "",
      "title" -> ""
    )

    "must return a 400 respone" in {
      val response = await(Movies.save().apply(request))

      response.header.status mustBe 400
    }
  }

  "Movies#edit" must {
    "present the correct movie" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        val result = Movies.edit(titanicID.value).apply(FakeRequest())
        val bodyText = contentAsString(result)

        bodyText must include ("Titanic")
      }
    }
  }

  "Movies#update with valid data" must {
    "update the record" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        val request = FakeRequest().withFormUrlEncodedBody(
          "id" -> titanicID.value.toString,
          "title" -> "T"
        )

        await(Movies.update(titanicID.value).apply(request))

        Movie.find(titanicID).get.title mustBe "T"
      }
    }

    "redirect to the movie's show page" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        val request = FakeRequest().withFormUrlEncodedBody(
          "id" -> titanicID.value.toString,
          "title" -> "T"
        )

        val response = Movies.update(titanicID.value).apply(request)
        val header = await(response).header

        header.status mustBe 303
        header.headers.get("location").get mustBe routes.Movies.show(titanicID).url
      }
    }
  }

  "Movies#update without a title" must {
    "return a 400 response" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        val request = FakeRequest().withFormUrlEncodedBody(
          "id" -> titanicID.value.toString,
          "title" -> ""
        )

        val response = await(Movies.update(titanicID.value).apply(request))

        response.header.status mustBe 400
      }
    }
  }

  "Movies#remove" must {
    "present the correct movie" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        val result = Movies.remove(titanicID.value).apply(FakeRequest())
        val bodyText = contentAsString(result)

        bodyText must include ("Titanic")
      }
    }
  }

  "Movies#delete" must {
    "delete 1 movie" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        await(Movies.delete(titanicID.value).apply(FakeRequest()))

        Movie.list.length mustBe 1
      }
    }

    "delete the correct movie" in {
      DB.withSession { implicit session =>
        val titanicID = Movie.query.filter(_.title === "Titanic").map(_.id).firstOption.get

        await(Movies.delete(titanicID.value).apply(FakeRequest()))

        Movie.find(titanicID) mustBe None
      }
    }
  }
}
