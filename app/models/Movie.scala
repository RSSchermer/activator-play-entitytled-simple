package models

import models.meta.Profile._
import models.meta.Profile.driver.api._
import models.meta.Schema._

case class Movie(
    id: Option[Long],
    title: String,
    directorID: Option[Long])(implicit includes: Includes[Movie])
  extends Entity[Movie, Long]
{
  val director = one(Movie.director)
}

object Movie extends EntityCompanion[Movies, Movie, Long] {
  val director = toOne[Directors, Director]
}
