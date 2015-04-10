package models

import models.meta.Profile._
import models.meta.Profile.driver.simple._
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
  val query = TableQuery[Movies]

  val director = toOne[Directors, Director](
    toQuery       = TableQuery[Directors],
    joinCondition = _.directorID === _.id)
}
