package models.meta

import models.meta.Profile._
import models.meta.Profile.driver.simple._
import models._

object Schema {
  class Directors(tag: Tag) extends EntityTable[Director, Long](tag, "DIRECTORS") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)

    def * = (id.?, name) <> ((Director.apply _).tupled, Director.unapply)
  }

  class Movies(tag: Tag) extends EntityTable[Movie, Long](tag, "MOVIES") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title", O.NotNull)
    def directorID = column[Long]("director_id", O.Nullable)

    def * = (id.?, title, directorID.?) <> ((Movie.apply _).tupled, Movie.unapply)

    def director = foreignKey("MOVIES_DIRECTOR_FK", directorID,
      TableQuery[Directors])(_.id, onDelete = ForeignKeyAction.SetNull)
  }
}
