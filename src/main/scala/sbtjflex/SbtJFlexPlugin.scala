/*
 * Copyright 2011 Steffen Fritzsche.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtjflex

import sbt._
import Process._
import Keys._
import jflex.Options
import jflex.Main
import scala.collection.JavaConversions._

object SbtJFlexPlugin extends AutoPlugin {

  final case class JFlexToolConfiguration(
    dot: Boolean = false,
    dump: Boolean = false,
    verbose: Boolean = false)

  final case class PluginConfiguration(grammarSuffix: String = ".flex")

  val jflex = config("jflex")
  val generate = TaskKey[Seq[File]]("generate")
  val jflexDependency = SettingKey[ModuleID]("jflex-dependency")
  val toolConfiguration = SettingKey[JFlexToolConfiguration]("jflex-tool-configuration")
  val pluginConfiguration = SettingKey[PluginConfiguration]("plugin-configuration")

  /**
    * use this if you don't want jflex to run automatically (because, e.g., you're checking it in)
    * you'll want to set [[target]] in [[jflex]] using [[unmanagedJflexSettings]] or your own variant
    */
  lazy val commonJflexSettings: Seq[Def.Setting[_]] = inConfig(jflex)(Seq(
    toolConfiguration := JFlexToolConfiguration(),
    pluginConfiguration := PluginConfiguration(),
    jflexDependency := "de.jflex" % "jflex" % "1.6.1",

    sourceDirectory := (sourceDirectory in Compile).value / "flex",

    managedClasspath := Classpaths.managedJars(jflex, (classpathTypes in jflex).value, update.value),

    generate := {
      val out = streams.value
      val options = (pluginConfiguration in jflex).value
      val cachedCompile = FileFunction.cached(out.cacheDirectory / "flex", inStyle = FilesInfo.lastModified, outStyle = FilesInfo.exists) { (in: Set[File]) =>
        generateWithJFlex(in, (target in jflex).value, (toolConfiguration in jflex).value, options, out.log)
      }
      cachedCompile(((sourceDirectory in jflex).value ** ("*" + options.grammarSuffix)).get.toSet).toSeq
    }
  )) ++ Seq(
    libraryDependencies += (jflexDependency in jflex).value,
    ivyConfigurations += jflex
  )

  lazy val unmanagedJflexSettings = commonJflexSettings ++ inConfig(jflex)(Seq(
    target in jflex := (javaSource in Compile).value,
    managedSources := (generate in jflex).value
  ))

  lazy val jflexSettings: Seq[Def.Setting[_]] = commonJflexSettings ++
  inConfig(jflex)(
    target in jflex := (sourceManaged in Compile).value
  ) ++ Seq(
    unmanagedSourceDirectories in Compile += (sourceDirectory in jflex).value,
    sourceGenerators in Compile += (generate in jflex).taskValue,
    cleanFiles += (target in jflex).value
  )

  private def generateWithJFlex(sources: Set[File], target: File, tool: JFlexToolConfiguration,
                                options: PluginConfiguration, log: Logger) = {
    printJFlexOptions(log, tool)

    // prepare target
    target.mkdirs()

    // configure jflex tool
    log.info("JFlex: Using JFlex version %s to generate source files.".format(Main.version))
    Options.dot = tool.dot
    Options.verbose = tool.verbose
    Options.dump = tool.dump
    Options.setDir(target.getPath)

    // process grammars
    val grammars = sources
    log.info("JFlex: Generating source files for %d grammars.".format(grammars.size))

    // add each grammar file into the jflex tool's list of grammars to process
    grammars foreach { g =>
      log.info("JFlex: Grammar file '%s' detected.".format(g.getPath))
      Main.generate(g)
    }

    (target ** ("*.java")).get.toSet
  }

  private def printJFlexOptions(log: Logger, options: JFlexToolConfiguration) {
    log.debug("JFlex: dump                : " + options.dump)
    log.debug("JFlex: dot                 : " + options.dot)
    log.debug("JFlex: verbose             : " + options.verbose)
  }

}
