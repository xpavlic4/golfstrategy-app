name := "reactivemongo-demo-app"

val buildVersion = "0.11.11"

version := buildVersion

resolvers += "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/"

scalaVersion := "2.11.7"

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % buildVersion

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala)
