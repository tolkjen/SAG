name := "warehouse"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.40-R8"

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true