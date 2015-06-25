package models.meta

import models.meta.Profile._
import models.meta.Profile.driver.api._
import models._

object Schema {
  class Directors(tag: Tag) extends EntityTable[Director, Long](tag, "directors") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = (id.?, name) <> ((Director.apply _).tupled, Director.unapply)
  }

  class Movies(tag: Tag) extends EntityTable[Movie, Long](tag, "movies") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def directorID = column[Long]("director_id")

    def * = (id.?, title, directorID.?) <> ((Movie.apply _).tupled, Movie.unapply)

    def director = foreignKey("movies_director_fk", directorID,
      TableQuery[Directors])(_.id, onDelete = ForeignKeyAction.SetNull)
  }
}
