resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.15")

libraryDependencies += "com.thesamet.scalapb" %% "sparksql-scalapb-gen" % "0.7.0"

