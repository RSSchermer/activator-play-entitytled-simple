package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._

import views._
import models._

object Directors extends Controller {
  val directorForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText
    )(Director.apply)(Director.unapply)
  )

  def list = DBAction { implicit rs =>
    Ok(html.directors.list(Director.list))
  }

  def show(id: Long) = DBAction { implicit rs =>
    Director.include(Director.movies).find(id) match {
      case Some(director) =>
        Ok(html.directors.show(director))
      case _ => NotFound
    }
  }

  def create = DBAction { implicit rs =>
    Ok(html.directors.create(directorForm))
  }

  def save = DBAction { implicit rs =>
    directorForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.directors.create(formWithErrors)),
      director => {
        Director.insert(director)
        Redirect(routes.Directors.list())
          .flashing("success" -> "The director was created successfully.")
      }
    )
  }

  def edit(id: Long) = DBAction { implicit rs =>
    Director.find(id) match {
      case Some(director) =>
        Ok(html.directors.edit(director.id.get, directorForm.fill(director)))
      case _ => NotFound
    }
  }

  def update(id: Long) = DBAction { implicit rs =>
    directorForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(html.directors.edit(id, formWithErrors)),
      director => {
        Director.update(director)
        Redirect(routes.Directors.show(id))
          .flashing("success" -> "The director was updated successfully.")
      }
    )
  }

  def remove(id: Long) = DBAction { implicit rs =>
    Director.find(id) match {
      case Some(director) => Ok(html.directors.remove(director))
      case _ => NotFound
    }
  }

  def delete(id: Long) = DBAction { implicit rs =>
    Director.delete(id)
    Redirect(routes.Directors.list())
      .flashing("success" -> "The director was deleted successfully.")
  }
}
