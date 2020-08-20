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
import jflex.option.Options
import jflex.Main
import scala.collection.JavaConversions._
import jflex.core.OptionUtils
import jflex.generator.LexGenerator

object SbtJFlexPlugin extends AutoPlugin {

  final case class JFlexToolConfiguration(
    dot: Boolean = false,
    dump: Boolean = false,
    verbose: Boolean = false)

  final case class PluginConfiguration(grammarSuffix: String = ".flex")

  val Jflex = config("jflex")
  val generate = TaskKey[Seq[File]]("generate")
  val jflexDependency = SettingKey[ModuleID]("jflex-dependency")
  val toolConfiguration = SettingKey[JFlexToolConfiguration]("jflex-tool-configuration")
  val pluginConfiguration = SettingKey[PluginConfiguration]("plugin-configuration")

  /**
    * use this if you don't want jflex to run automatically (because, e.g., you're checking it in)
    * you'll want to set [[target]] in [[jflex]] using [[unmanagedJflexSettings]] or your own variant
    */
  lazy val commonJflexSettings: Seq[Def.Setting[_]] = inConfig(Jflex)(Seq(
    toolConfiguration := JFlexToolConfiguration(),
    pluginConfiguration := PluginConfiguration(),
    jflexDependency := "de.jflex" % "jflex" % "1.6.1",

    sourceDirectory := (sourceDirectory in Compile).value / "flex",

    managedClasspath := Classpaths.managedJars(Jflex, (classpathTypes in Jflex).value, update.value),

    generate := {
      val out = streams.value
      val options = (pluginConfiguration in Jflex).value
      val cachedCompile = FileFunction.cached(out.cacheDirectory / "flex", inStyle = FilesInfo.lastModified, outStyle = FilesInfo.exists) { (in: Set[File]) =>
        generateWithJFlex(in, (target in Jflex).value, (toolConfiguration in Jflex).value, options, out.log)
      }
      cachedCompile(((sourceDirectory in Jflex).value ** ("*" + options.grammarSuffix)).get.toSet).toSeq
    }
  )) ++ Seq(
    libraryDependencies += (jflexDependency in Jflex).value,
    ivyConfigurations += Jflex
  )

  lazy val unmanagedJflexSettings = commonJflexSettings ++ inConfig(Jflex)(Seq(
    target in Jflex := (javaSource in Compile).value,
    managedSources := (generate in Jflex).value
  ))

  lazy val jflexSettings: Seq[Def.Setting[_]] = commonJflexSettings ++
  inConfig(Jflex)(
    target in Jflex := (sourceManaged in Compile).value
  ) ++ Seq(
    unmanagedSourceDirectories in Compile += (sourceDirectory in Jflex).value,
    sourceGenerators in Compile += (generate in Jflex).taskValue,
    cleanFiles += (target in Jflex).value
  )

  private def generateWithJFlex(sources: Set[File], target: File, tool: JFlexToolConfiguration,
                                options: PluginConfiguration, log: Logger) = {
    printJFlexOptions(log, tool)

    // prepare target
    target.mkdirs()

    // configure jflex tool
    log.info(s"JFlex: Using JFlex version ${jflex.base.Build.VERSION} to generate source files.")
    Options.dot = tool.dot
    Options.verbose = tool.verbose
    Options.dump = tool.dump
    OptionUtils.setDir(target.getPath)

    // process grammars
    val grammars = sources
    log.info("JFlex: Generating source files for %d grammars.".format(grammars.size))

    // add each grammar file into the jflex tool's list of grammars to process
    grammars foreach { g =>
      log.info("JFlex: Grammar file '%s' detected.".format(g.getPath))
      new LexGenerator(g).generate()
    }

    (target ** ("*.java")).get.toSet
  }

  private def printJFlexOptions(log: Logger, options: JFlexToolConfiguration) {
    log.debug("JFlex: dump                : " + options.dump)
    log.debug("JFlex: dot                 : " + options.dot)
    log.debug("JFlex: verbose             : " + options.verbose)
  }

}
