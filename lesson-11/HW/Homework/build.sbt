import sbt.ClassLoaderLayeringStrategy

ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization := "local"
ThisBuild / version      := "0.1.0"

ThisBuild / fork := true
ThisBuild / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
ThisBuild / javaOptions ++= Seq(
  "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
)

lazy val root = (project in file("."))
  .settings(
    name := "Homework",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % "3.5.1"
    )
  )