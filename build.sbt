name := """play-test"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "react" % "0.13.3",
  "org.webjars" % "marked" % "0.3.2",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap" % "3.3.5",
	"org.reactivemongo" %% "play2-reactivemongo" % "0.11.6.play24",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.1",
  "com.sksamuel.scrimage" %% "scrimage-io" % "2.1.0.M2"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
