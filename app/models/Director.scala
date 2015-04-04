package models

import models.meta.Profile._
import models.meta.Profile.driver.simple._
import models.meta.Schema._

case class Director(
    id: Option[Long],
    name: String)(implicit includes: Includes[Director])
  extends Entity[Director]
{
  type IdType = Long

  val movies = many(Director.movies)
}

object Director extends EntityCompanion[Directors, Director] {
  val query = TableQuery[Directors]

  val movies = toMany[Movies, Movie](
    toQuery       = TableQuery[Movies],
    joinCondition = _.id === _.directorID)
}
