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

import scala.concurrent.Future

object Directors extends Controller {
  val directorForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText
    )(Director.apply)(Director.unapply)
  )

  def list = Action.async { implicit rs =>
    db.run(Director.all.result).map { directors =>
      Ok(html.directors.list(directors))
    }
  }

  def show(id: Long) = Action.async { implicit rs =>
    db.run(Director.one(id).include(Director.movies).result).map {
      case Some(director) =>
        Ok(html.directors.show(director))
      case _ => NotFound
    }
  }

  def create = Action { implicit rs =>
    Ok(html.directors.create(directorForm))
  }

  def save = Action.async { implicit rs =>
    directorForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest(html.directors.create(formWithErrors))),
      director =>
        db.run(Director.insert(director)).map { _ =>
          Redirect(routes.Directors.list())
            .flashing("success" -> "The director was created successfully.")
        }
    )
  }

  def edit(id: Long) = Action.async { implicit rs =>
    db.run(Director.one(id).result).map {
      case Some(director) =>
        Ok(html.directors.edit(director.id.get, directorForm.fill(director)))
      case _ => NotFound
    }
  }

  def update(id: Long) = Action.async { implicit rs =>
    directorForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest(html.directors.edit(id, formWithErrors))),
      director =>
        db.run(Director.update(director)).map { _ =>
          Redirect(routes.Directors.show(id))
            .flashing("success" -> "The director was updated successfully.")
        }
    )
  }

  def remove(id: Long) = Action.async { implicit rs =>
    db.run(Director.one(id).result).map {
      case Some(director) => Ok(html.directors.remove(director))
      case _ => NotFound
    }
  }

  def delete(id: Long) = Action.async { implicit rs =>
    db.run(Director.delete(id)).map { _ =>
      Redirect(routes.Directors.list())
        .flashing("success" -> "The director was deleted successfully.")
    }
  }
}
