name := "akka-mastodon"

version := "0.1"

scalaVersion := "2.12.1"

scalacOptions ++= Seq("-encoding", "UTF-8")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "io.circe" %% "circe-core" % "0.7.1",
  "io.circe" %% "circe-generic" % "0.7.1",
  "io.circe" %% "circe-parser" % "0.7.1"
)
