package part2structuredstreaming

import org.apache.spark.sql.{Column, SparkSession}
import org.apache.spark.sql.functions.{col, sum}

object StreamingAggregations {
  val spark: SparkSession = SparkSession.builder()
    .appName("StreamingAggregations")
    .config("spark.master", "local[2]")
    .getOrCreate()

  def streamingCount() = {
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()

    val lineCount = lines.selectExpr("count(*) as lineCount")
    // agg with distinct are not supported
    // otherwise spark will need to keep track of everything
    lineCount.writeStream
      .format("console")
      .outputMode("complete") // append & update not supported on agg without watermark
      .start()
      .awaitTermination()
  }

  def numericalAgg(aggFunc: Column => Column): Unit = {
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()

    val numbers = lines.select(col("value").cast("integer").as("number"))
    val aggregationsDF = numbers.select(aggFunc(col("number")).as("agg_so_far"))
    aggregationsDF.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  def groupNames() = {
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()

    val names = lines
      .select(col("value") as "name")
      .groupBy(col("name")) // relational grouped dataset
      .count()

    names.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    //    streamingCount()
    //    numericalAgg(sum)
    groupNames()

  }
}
