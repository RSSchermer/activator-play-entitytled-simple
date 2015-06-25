package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import views._
import models._
import models.meta.Profile._
import models.meta.Profile.driver.api._

object Movies extends Controller {
  val movieForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "directorID" -> optional(longNumber)
    )(Movie.apply)(Movie.unapply)
  )

  def list = Action.async { implicit rs =>
    db.run(Movie.all.include(Movie.director).result).map { directors =>
      Ok(html.movies.list(directors))
    }
  }

  def show(id: Long) = Action.async { implicit rs =>
    db.run(Movie.one(id).include(Movie.director).result).map {
      case Some(movie) =>
        Ok(html.movies.show(movie))
      case _ => NotFound
    }
  }

  def create = Action.async { implicit rs =>
    db.run(Director.all.result).map { directors =>
      Ok(html.movies.create(movieForm, directors))
    }
  }

  def save = Action.async { implicit rs =>
    movieForm.bindFromRequest.fold(
      formWithErrors =>
        db.run(Director.all.result).map { directors =>
          BadRequest(html.movies.create(formWithErrors, directors))
        },
      movie =>
        db.run(Movie.insert(movie)).map { _ =>
          Redirect(routes.Movies.list())
            .flashing("success" -> "The movie was created successfully.")
        }
    )
  }

  def edit(id: Long) = Action.async { implicit rs =>
    for {
      movieOption <- db.run(Movie.one(id).result)
      directors <- db.run(Director.all.result)
    } yield movieOption match {
      case Some(movie) =>
        Ok(html.movies.edit(movie.id.get, movieForm.fill(movie), directors))
      case _ => NotFound
    }
  }

  def update(id: Long) = Action.async { implicit rs =>
    movieForm.bindFromRequest.fold(
      formWithErrors =>
        db.run(Director.all.result).map { directors =>
          BadRequest(html.movies.edit(id, formWithErrors, directors))
        },
      movie =>
        db.run(Movie.update(movie)).map { _ =>
          Redirect(routes.Movies.show(id))
            .flashing("success" -> "The movie was updated successfully.")
        }
    )
  }

  def remove(id: Long) = Action.async { implicit rs =>
    db.run(Movie.one(id).result).map {
      case Some(movie) => Ok(html.movies.remove(movie))
      case _ => NotFound
    }
  }

  def delete(id: Long) = Action.async { implicit rs =>
    db.run(Movie.delete(id)).map { _ =>
      Redirect(routes.Movies.list())
        .flashing("success" -> "The movie was deleted successfully.")
    }
  }
}
