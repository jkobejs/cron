ThisBuild / scalaVersion     := "2.13.8"

lazy val root = (project in file("."))
  .aggregate(zio1, zio2)
  .settings(publish := false)

val zio1Version = "1.0.15"
val zio2Version = "2.0.0"

val scalaVersions = List("2.13.8", "2.12.10", "3.1.3")

lazy val zio1 = (project in file("zio1")).settings(
  name := "zio1-cron",
  libraryDependencies ++= Seq(
    "dev.zio"                %% "zio"                % zio1Version,
    "com.cronutils"           % "cron-utils"         % "9.1.6",
    "dev.zio"                %% "zio-test"           % zio1Version % Test,
    "dev.zio"                %% "zio-test-sbt"       % zio1Version % Test
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  crossScalaVersions := scalaVersions
)

lazy val zio2 = (project in file("zio2")).settings(
  name := "zio-cron",
  libraryDependencies ++= Seq(
    "dev.zio"                %% "zio"                % zio2Version,
    "com.cronutils"           % "cron-utils"         % "9.1.6",
    "dev.zio"                %% "zio-test"           % zio2Version % Test,
    "dev.zio"                %% "zio-test-sbt"       % zio2Version % Test
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  crossScalaVersions := scalaVersions
)

inThisBuild(
  List(
    organization := "io.github.jkobejs",
    homepage     := Some(url("https://github.com/jkobejs/cron")),
    licenses     := List("MIT" -> url("https://github.com/jkobejs/cron/LICENSE")),
    developers := List(
      Developer(
        "jkobejs",
        "Josip Grgurica",
        "josip.grgurica@protonmail.com",
        url("https://github.com/jkobejs")
      )
    )
  )
)
