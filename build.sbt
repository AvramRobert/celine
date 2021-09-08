val scala3Version = "3.0.0"
val javalinVersion = "3.13.6"
val slf4jVersion = "1.7.30"
val nemesisVersion = "0.2.0"
val shttpVersion = "3.3.3"
val commonsTextVersion = "1.9"
val zioVersion = "1.0.8"
val configVersion = "1.4.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "orchestra",
    version := "0.1.0",

    scalaVersion := scala3Version,

    resolvers += Resolver.mavenLocal,

    libraryDependencies ++= Seq(
      "io.javalin" % "javalin" % javalinVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      "com.ravram" % "nemesis" % nemesisVersion,
      "com.softwaremill.sttp.client3" %% "core" % shttpVersion,
      "org.apache.commons" % "commons-text" % commonsTextVersion,
      "dev.zio" %% "zio" % zioVersion,
      "com.typesafe" % "config" % configVersion),

    assembly / assemblyMergeStrategy := {
      case PathList("org", "eclipse", "jetty", "http", _*) => MergeStrategy.first
      case other => (assembly / assemblyMergeStrategy).value(other)
    })
