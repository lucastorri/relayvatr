import sbt._
import sbt.Keys._
import sbt.Project.project

object RelayvatrBuild extends Build {

  val appVersion = "1.0"

  val defaultScalaVersion = "2.11.7"
  val defaultJavaVersion = "1.8"

  val defaultScalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:dynamics",
    "-language:higherKinds",
    "-language:existentials",
    "-language:experimental.macros",
    "-Xmax-classfile-name", "140",
    "-Xlint",
    s"-target:jvm-$defaultJavaVersion"
  )

  val defaultJavacOptions = Seq(
    "-source", defaultJavaVersion,
    "-target", defaultJavaVersion
  )

  val baseSettings = Defaults.coreDefaultSettings ++
    Seq(
      scalacOptions ++= defaultScalacOptions,
      scalaVersion := defaultScalaVersion,
      javacOptions in Compile ++= defaultJavacOptions
    )

  lazy val root = project.in(file("."))
    .settings(dependencies(
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
      "io.reactivex" %% "rxscala" % "0.26.0",
      "com.typesafe.akka" %% "akka-actor" % "2.4.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "ch.qos.logback" % "logback-classic" % "1.1.3"
    ))
    .settings(baseSettings: _*)

  def dependencies(moduleIds: ModuleID*) = {
    libraryDependencies ++= moduleIds
  }

}
