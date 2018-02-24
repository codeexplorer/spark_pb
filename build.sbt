name := "sparkpb"
version := "0.0.0-SNAPSHOT"
organization := "com.codeexplorer.sparkpb"

scalaVersion := "2.11.11"
val sparkVersion = "2.2.1"
val scalaPbVersion = "0.7.0"

val excludeScalaLang = ExclusionRule(organization = "org.scala-lang")
val excludeNettyIo = ExclusionRule(organization = "org.jboss.netty")
val excludeSparkProject = ExclusionRule(organization = "org.spark-project.spark")

//resolvers += "Mr.Powers" at "https://dl.bintray.com/spark-packages/maven/"
dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql"  % sparkVersion % Provided,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion excludeAll(
    ExclusionRule(organization = "org.spark-project.spark", name = "unused"),
    ExclusionRule(organization = "org.apache.spark", name = "spark-sql"),
    ExclusionRule(organization = "org.apache.hadoop")
  ),
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion excludeAll(
    ExclusionRule(organization = "org.spark-project.spark", name = "unused"),
    ExclusionRule(organization = "org.apache.spark", name = "spark-streaming"),
    ExclusionRule(organization = "org.apache.hadoop")
  ),
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalaPbVersion,
  "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.7.0"
)
PB.protoSources.in(Compile) := Seq(sourceDirectory.in(Compile).value / "protobufs")
PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value,
  new scalapb.UdtGenerator(flatPackage = false) -> (sourceManaged in Compile).value)


