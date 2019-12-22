name := "jdbc-engine"

version := "0.1"

scalaVersion := "2.12.9"

scalacOptions += "-Ypartial-unification"

// Scala 2.10, 2.11, 2.12
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.pauldijou" %% "jwt-core" % "3.0.1",
  "de.heikoseeberger" %% "akka-http-json4s" % "1.22.0",
  "org.json4s" %% "json4s-native" % "3.6.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.json4s" %% "json4s-jackson" % "3.6.7",
  "org.json4s" %% "json4s-ext" % "3.6.7",

  // for scalikejdbc
  "org.scalikejdbc" %% "scalikejdbc"       % "3.2.4",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "3.2.4"   % "test",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "3.2.4",
  "org.scalikejdbc" %% "scalikejdbc-streams" % "3.2.4",
  "org.scalikejdbc" %% "scalikejdbc-joda-time" % "3.2.4",
  "com.h2database"  %  "h2"                % "1.4.199",
  "mysql" % "mysql-connector-java" % "8.0.17",
  "com.zaxxer" % "HikariCP" % "3.3.1",
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "io.monix" %% "monix" % "3.0.0-RC3",
  "org.typelevel" %% "cats-core" % "2.0.0-M4",
  "com.github.tasubo" % "jurl-tools" % "0.6"
)