ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "spark-rockjvm-streaming"
  )
//resolvers ++= Seq(
//  "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven",
//  "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases",
//  "MavenRepository" at "https://mvnrepository.com"
//)
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.2.1",
  "org.apache.spark" %% "spark-sql" % "3.2.1",

  // streaming
  "org.apache.spark" %% "spark-streaming" % "3.2.1",

  // streaming-kafka
  "org.apache.spark" % "spark-sql-kafka-0-10_2.12" % "3.2.1",

  // low-level integrations
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.2.1",
  "org.apache.spark" %% "spark-streaming-kinesis-asl" % "3.2.1",

  "com.datastax.spark" %% "spark-cassandra-connector" % "3.2.0",

  // postgres
  "org.postgresql" % "postgresql" % "42.3.6",

  // logging
  "org.apache.logging.log4j" % "log4j-api" % "2.17.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.17.2",

  // kafka
  "org.apache.kafka" %% "kafka" % "3.1.0",
  "org.apache.kafka" % "kafka-streams" % "3.1.0"
)