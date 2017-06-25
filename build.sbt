name := "reactivemongo-demo-app"

val buildVersion = "0.12.4"

version := buildVersion

resolvers += "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/"

scalaVersion := "2.11.9"

libraryDependencies ++= Seq(
  //"com.typesafe.play" %% "play-iteratees" % "2.5.15",
  ("org.reactivemongo" %% "play2-reactivemongo" % buildVersion).
    exclude("com.typesafe.play", "*")
)

routesGenerator := InjectedRoutesGenerator

fork in run := true

lazy val root = (project in file(".")).enablePlugins(PlayScala)
