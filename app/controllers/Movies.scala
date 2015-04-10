package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._

import views._
import models._

object Movies extends Controller {
  val movieForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "directorID" -> optional(longNumber)
    )(Movie.apply)(Movie.unapply)
  )

  def list = DBAction { implicit rs =>
    Ok(html.movies.list(Movie.include(Movie.director).list))
  }

  def show(id: Long) = DBAction { implicit rs =>
    Movie.include(Movie.director).find(id) match {
      case Some(movie) =>
        Ok(html.movies.show(movie))
      case _ => NotFound
    }
  }

  def create = DBAction { implicit rs =>
    Ok(html.movies.create(movieForm))
  }

  def save = DBAction { implicit rs =>
    movieForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.movies.create(formWithErrors)),
      movie => {
        Movie.insert(movie)
        Redirect(routes.Movies.list())
          .flashing("success" -> "The movie was created successfully.")
      }
    )
  }

  def edit(id: Long) = DBAction { implicit rs =>
    Movie.find(id) match {
      case Some(movie) =>
        Ok(html.movies.edit(movie.id.get, movieForm.fill(movie)))
      case _ => NotFound
    }
  }

  def update(id: Long) = DBAction { implicit rs =>
    movieForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(html.movies.edit(id, formWithErrors)),
      movie => {
        Movie.update(movie)
        Redirect(routes.Movies.show(id))
          .flashing("success" -> "The movie was updated successfully.")
      }
    )
  }

  def remove(id: Long) = DBAction { implicit rs =>
    Movie.find(id) match {
      case Some(movie) => Ok(html.movies.remove(movie))
      case _ => NotFound
    }
  }

  def delete(id: Long) = DBAction { implicit rs =>
    Movie.delete(id)
    Redirect(routes.Movies.list())
      .flashing("success" -> "The movie was deleted successfully.")
  }
}
