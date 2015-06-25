name := """play-entitytled-simple"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.play"        %% "play-slick"             % "1.0.0",
  "com.typesafe.play"        %% "play-slick-evolutions"  % "1.0.0",
  "com.github.rsschermer"    %% "entitytled-core"        % "0.7.0",
  "com.h2database"           %  "h2"                     % "1.4.177",
  "org.scalatest"            %% "scalatest"              % "2.2.5"     % "test",
  "org.scalatestplus"        %% "play"                   % "1.4.0-M3"  % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

fork in run := true

parallelExecution in Test := false
fork in test := false