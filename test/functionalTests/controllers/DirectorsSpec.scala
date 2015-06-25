package functionalTests.controllers

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import models.meta.Profile._
import models.meta.Profile.driver.api._
import models._
import controllers._

class DirectorsSpec extends BaseSpec {
  before {
    await(db.run(DBIO.seq(
      Director.insert(Director(None, "Alfred Hitchcock")),
      Director.insert(Director(None, "Steven Spielberg"))
    )))
  }

  after {
    // Delete instead of truncate because of foreign key constraint
    await(db.run(sqlu"""delete from "directors""""))
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
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      val result = Directors.show(hitchcockID.value).apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Alfred Hitchcock")
    }
  }

  "Directors#save with valid data" must {
    val request = FakeRequest().withFormUrlEncodedBody(
      "id" -> "",
      "name" -> "Martin Scorcese"
    )

    "create a new record" in {
      await(Directors.save().apply(request))

      val directorCount = await(db.run(Director.all.size.result))

      directorCount mustBe 3
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
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      val result = Directors.edit(hitchcockID.value).apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Alfred Hitchcock")
    }
  }

  "Directors#update with valid data" must {
    "update the record" in {
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      val request = FakeRequest().withFormUrlEncodedBody(
        "id" -> hitchcockID.value.toString,
        "name" -> "Alfed H."
      )

      await(Directors.update(hitchcockID.value).apply(request))

      await(db.run(Director.one(hitchcockID).result)).get.name mustBe "Alfed H."
    }

    "redirect to the director's show page" in {
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

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

  "Directors#update without a name" must {
    "return a 400 response" in {
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      val request = FakeRequest().withFormUrlEncodedBody(
        "id" -> hitchcockID.value.toString,
        "name" -> ""
      )

      val response = await(Directors.update(hitchcockID.value).apply(request))

      response.header.status mustBe 400
    }
  }

  "Directors#remove" must {
    "present the correct director" in {
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      val result = Directors.remove(hitchcockID.value).apply(FakeRequest())
      val bodyText = contentAsString(result)

      bodyText must include ("Alfred Hitchcock")
    }
  }

  "Directors#delete" must {
    "delete 1 director" in {
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      await(Directors.delete(hitchcockID.value).apply(FakeRequest()))

      await(db.run(Director.all.length.result)) mustBe 1
    }

    "delete the correct director" in {
      val hitchcockID = await(db.run(Director.all.filter(_.name === "Alfred Hitchcock").map(_.id).result)).head

      await(Directors.delete(hitchcockID.value).apply(FakeRequest()))

      await(db.run(Director.one(hitchcockID).result)) mustBe None
    }
  }
}
