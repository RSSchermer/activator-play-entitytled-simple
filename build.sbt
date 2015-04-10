name := """play-entitytled-simple"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.play"             %% "play-slick"          % "0.8.0",
  "com.github.rsschermer"         %% "entitytled-core"     % "0.4.1",
  "org.scalatestplus"             %% "play"                % "1.1.0"   % "test"
)

fork in Test := false

lazy val root = (project in file(".")).enablePlugins(PlayScala)
