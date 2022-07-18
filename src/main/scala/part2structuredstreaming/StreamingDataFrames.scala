package part2structuredstreaming

import common.stocksSchema
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, length}
import org.apache.spark.sql.streaming.Trigger
import concurrent.duration._

object StreamingDataFrames {
  val spark = SparkSession.builder()
    .appName("StreamingDataFrames")
    .config("spark.master", "local[2]")
    .getOrCreate()

  def readFromSocket() = {
    // read a DF
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "12345")
      .load()

    // transformations
    val shorLines = lines.filter(length(col("value")) <= 5)

    // tell between a static vs a streaming DF
    //     shorLines.isStreaming


    // Consuming a DF
    val query = shorLines.writeStream
      .format("console")
      .outputMode("append")
      .start()

    // wait for the stream to finish
    query.awaitTermination()
  }

  def readFromFiles() = {
    val stocksDF = spark.readStream
      .format("csv")
      .option("header", "false")
      .option("dateFormat", "MMM d yyyy")
      .schema(stocksSchema)
      .load("src/main/resources/data/stocks")

    stocksDF.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def demoTriggers() = {
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()

    // write the lines DF at a certain trigger
    lines.writeStream
      .format("console")
      .outputMode("append")
      .trigger(
//        Trigger.ProcessingTime(2.seconds)  // every 2 seconds run the query
//        Trigger.Once() // single batch, then terminate
        Trigger.Continuous(2.seconds) // experimental `2020` , every 2 seconds create a batch
      )
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    //    readFromSocket()
//    readFromFiles()
    demoTriggers()
  }

}
