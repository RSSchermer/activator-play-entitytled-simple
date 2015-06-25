package functionalTests.controllers

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import models.meta.Profile._
import models.meta.Profile.driver.api._
import models._
import controllers._

class MoviesSpec extends BaseSpec {
  before {
    await(db.run(for {
      hithcockID <- Director.insert(Director(None, "Alfred Hitchcock"))
      cameronID <- Director.insert(Director(None, "James Cameron"))

      _ <- Movie.insert(Movie(None, "Titanic", Some(cameronID)))
      _ <- Movie.insert(Movie(None, "Vertigo", Some(hithcockID)))
    } yield ()))
  }

  after {
    // Delete instead of truncate because of foreign key constraint
    await(db.run(DBIO.seq(
      sqlu"""delete from "directors"""",
      sqlu"""delete from "movies""""
    )))
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
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      val result = Movies.show(titanicID.value).apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Titanic")
    }
  }

  "Movies#save with valid data" must {
    val request = FakeRequest().withFormUrlEncodedBody(
      "id" -> "",
      "title" -> "E.T."
    )

    "create a new record" in {
      await(Movies.save().apply(request))

      await(db.run(Movie.all.length.result)) mustBe 3
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
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      val result = Movies.edit(titanicID.value).apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Titanic")
    }
  }

  "Movies#update with valid data" must {
    "update the record" in {
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      val request = FakeRequest().withFormUrlEncodedBody(
        "id" -> titanicID.value.toString,
        "title" -> "T"
      )

      await(Movies.update(titanicID.value).apply(request))

      await(db.run(Movie.one(titanicID).result)).get.title mustBe "T"
    }

    "redirect to the movie's show page" in {
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

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

  "Movies#update without a title" must {
    "return a 400 response" in {
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      val request = FakeRequest().withFormUrlEncodedBody(
        "id" -> titanicID.value.toString,
        "title" -> ""
      )

      val response = await(Movies.update(titanicID.value).apply(request))

      response.header.status mustBe 400
    }
  }

  "Movies#remove" must {
    "present the correct movie" in {
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      val result = Movies.remove(titanicID.value).apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Titanic")
    }
  }

  "Movies#delete" must {
    "delete 1 movie" in {
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      await(Movies.delete(titanicID.value).apply(FakeRequest()))

      await(db.run(Movie.all.length.result)) mustBe 1
    }

    "delete the correct movie" in {
      val titanicID = await(db.run(Movie.all.filter(_.title === "Titanic").map(_.id).result)).head

      await(Movies.delete(titanicID.value).apply(FakeRequest()))

      await(db.run(Movie.one(titanicID).result)) mustBe None
    }
  }
}
