package part2structuredstreaming

import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object StreamingJoin {
  val src = "src/main/resources/data"
  val spark = SparkSession.builder()
    .appName("StreamingJoin")
    .config("spark.master", "local[2]")
    .getOrCreate()

  val guitarPlayers = spark.read
    .option("inferSchema", "true")
    .json(s"$src/guitarPlayers")
  val guitars = spark.read
    .option("inferSchema", "true")
    .json(s"$src/guitars")
  val bands = spark.read
    .option("inferSchema", "true")
    .json(s"$src/bands")

  // joins statics DF
  val joinCondition: Column = guitarPlayers.col("band") === bands.col("id")
  val guitaristsBands: DataFrame = guitarPlayers
    .join(bands, joinCondition, "inner")

  val bandSchema = bands.schema

  def joinStreamWithStatic() = {
    val streamBandsDF = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()
      .select(from_json(col("value"), bandSchema) as "band")
      .selectExpr(
        "band.id as id",
        "band.name as name",
        "band.hometown as hometown",
        "band.year as year"
      )
    // joins happens peer batch
    val streamedBandsGuitaristsDF = streamBandsDF
      .join(guitarPlayers,
        guitarPlayers.col("band") === streamBandsDF.col("id"),
        "inner"
      )
    /*
    * restricted joins
    * - stream join with static: RIGHT outer join/full outer join/right_semi not permitted
    * - static DF join  with streaming: LEFT outer join/full/left_semi not permitted
    * */
    streamedBandsGuitaristsDF.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  // sink spark 2.3 we have stream vs stream joins
  def joinStreamWithStream() = {
    val streamBandsDF = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()
      .select(from_json(col("value"), bandSchema) as "band")
      .selectExpr(
        "band.id as id",
        "band.name as name",
        "band.hometown as hometown",
        "band.year as year"
      )
    val streamedGuitaristsDF = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12346)
      .load()
      .select(from_json(col("value"), guitarPlayers.schema) as "guitarPlayer")
      .selectExpr(
        "guitarPlayer.id as id",
        "guitarPlayer.name as name",
        "guitarPlayer.guitars as guitars",
        "guitarPlayer.band as band"
      )

    val streamJoin = streamBandsDF
      .join(
        streamedGuitaristsDF,
        streamedGuitaristsDF.col("band") === streamBandsDF.col("id"))

    /*
    * - inner join are supported
    * - left/right outer joins are supported, but MUST have a watermark
    * - full outer joins are not supported*/
    streamJoin.writeStream
      .format("console")
      .outputMode("append") // only append supported for stream vs stream join
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    joinStreamWithStream()
  }
}
