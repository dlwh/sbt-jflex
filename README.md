# sbt-jflex

A plugin for sbt-0.11.x that generates code based on an jflex specification.

This is a shameless fork-and-adapt of stefri's sbt-antlr plugin.


## Usage

TODO

Depend on the plugin: `./project/plugins/build.sbt`

    resolvers += "stefri" at "http://stefri.github.com/repo/snapshots"

    addSbtPlugin("com.github.stefri" % "sbt-antlr" % "0.2-SNAPSHOT")

Place your ANTLR3 grammar files in `src/main/antlr3` and they will be
included in your next build. Note, `sbt-antlr` generates the source code
only once as long as your grammar file didn't change it does not
re-generate the java source files.


## Include Plugin Settings

Include the settings from `sbtjflex.SbtJFlexPlugin.antlrSettings` in
your project build file. See the [SBT wiki page on plugins][1] for
further details.


## Problems and Feature Requests

Please use the issue tracker on github if you find a bug or want to
request a specific feature. Note, this plugin is in early alpha, there
are still lots of things todo - feel free to fork and send a pull
request to improve the codebase.


## License

`sbt-jflex` is licensed under the [Apache 2.0 License][2],
see the `LICENSE.md` file for further details.


## Credits

This is a shameless fork-and-adapt of stefri's sbt-antlr plugin.
