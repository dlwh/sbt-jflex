import sbt._
import Keys._

object Dependencies {
    val jflex = "de.jflex" % "jflex" % "1.6.0" % "compile"

    val commonDeps = Seq(
        jflex
    )
}

object BuildSbtJFlex extends Build {
    import Dependencies._

    val sbtAntlr = Project(
        id = "sbt-jflex",
        base = file("."),

        settings = Defaults.defaultSettings ++ Seq(
            organization := "org.scalanlp",
            version := "0.3",
            sbtPlugin := true,
            
            scalacOptions := Seq("-deprecation", "-unchecked"),

            libraryDependencies ++= commonDeps,

            publishTo <<= version { (v: String) =>
              val nexus = "https://oss.sonatype.org/"
              if (v.trim.endsWith("SNAPSHOT")) 
                Some("snapshots" at nexus + "content/repositories/snapshots") 
                else
                  Some("releases"  at nexus + "service/local/staging/deploy/maven2")
                },
                publishArtifact in Test := false,
                pomIncludeRepository := { _ => false },
                pomExtra := (
                  <url>http://scalanlp.org/</url>
                  <licenses>
                    <license>
                      <name>Apache 2</name>
                      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
                      <distribution>repo</distribution>
                    </license>
                  </licenses>
                  <scm>
                    <url>git@github.com:dlwh/breeze.git</url>
                    <connection>scm:git:git@github.com:dlwh/breeze.git</connection>
                  </scm>
                  <developers>
                    <developer>
                      <id>dlwh</id>
                      <name>David Hall</name>
                      <url>http://cs.berkeley.edu/~dlwh/</url>
                    </developer>
                  </developers>
                )

        ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings
    )
}
