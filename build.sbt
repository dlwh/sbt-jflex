val sbtJflex = (project in file(".")).settings(
  name := "sbt-jflex",
  organization := "org.scalanlp",
  version := "1.0",
  sbtPlugin := true,
  description := "automatically compile jflex grammars",

  scalacOptions := Seq("-deprecation", "-unchecked"),

  libraryDependencies ++= Seq("de.jflex" % "jflex" % "1.8.2" % "compile"),

  publishTo := {
    val nexus = "https://oss.sonatype.org"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at s"$nexus/content/repositories/snapshots")
    else
      Some("releases"  at s"$nexus/service/local/staging/deploy/maven2")
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
  ),

  crossSbtVersions := Seq("0.13.18", "1.3.13")
)
