package models

import models.meta.Profile._
import models.meta.Profile.driver.api._
import models.meta.Schema._

case class Director(
    id: Option[Long],
    name: String)(implicit includes: Includes[Director])
  extends Entity[Director, Long]
{
  val movies = many(Director.movies)
}

object Director extends EntityCompanion[Directors, Director, Long] {
  val movies = toMany[Movies, Movie]
}
