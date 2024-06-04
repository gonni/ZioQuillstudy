ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.1"

lazy val root = (project in file("."))
  .settings(
    name := "ZioQuillStudy"
  )

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.0-RC3",
  "dev.zio"       %% "zio-json"            % "0.6.2",
  "dev.zio"       %% "zio-http"            % "3.0.0-RC6",
  "io.getquill"   %% "quill-zio"           % "4.8.0",
  "io.getquill"   %% "quill-jdbc-zio"      % "4.8.0",
  "com.h2database" % "h2"                  % "2.2.224",
  "mysql" % "mysql-connector-java" % "8.0.27",
  "dev.zio"       %% "zio-config"          % "4.0.0-RC16",
  "dev.zio"       %% "zio-config-typesafe" % "4.0.0-RC16",
  "dev.zio"       %% "zio-config-magnolia" % "4.0.0-RC16",
  "dev.zio"       %% "zio-logging"       % "2.1.15",
  "dev.zio"       %% "zio-logging-slf4j" % "2.1.15",
  "org.slf4j"      % "slf4j-simple"      % "2.0.9"
)


// SLF4j v2 integration
//libraryDependencies += "dev.zio" %% "zio-logging-slf4j2" % "2.3.0"
//// Using ZIO Logging for SLF4j v2 loggers, usually third-party non-ZIO libraries
//libraryDependencies += "dev.zio" %% "zio-logging-slf4j2-bridge" % "2.3.0"
//libraryDependencies += "dev.zio" %% "zio-logging-jul-bridge" % "2.3.0"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0"