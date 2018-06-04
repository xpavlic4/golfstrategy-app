name := "reactivemongo-demo-app"

val buildVersion = "0.14.0-play26-SNAPSHOT"

version := buildVersion

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/")

scalacOptions in Compile += "-target:jvm-1.8"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
  "org.reactivemongo" %% "play2-reactivemongo" % buildVersion
)

routesGenerator := InjectedRoutesGenerator

fork in run := true

lazy val root = (project in file(".")).enablePlugins(PlayScala)
