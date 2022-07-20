package part2structuredstreaming

import common.{Car, carsSchema}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}
import org.apache.spark.sql.functions._

object StreamingDatasets {
  val spark = SparkSession.builder()
    .appName("StreamingDatasets")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  def readCars(): Dataset[Car] = {
    // useful for DF -> DS transformation
    val carEncoder = Encoders.product[Car]
    spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()
      .select(from_json(col("value"), carsSchema) as "cars")
      .selectExpr("cars.*") // DF with multiples columns
      .as[Car](carEncoder) // if you no use encoder you need import spark.implicits._
  }

  def showCarsNames() = {
    val carsDS = readCars()
    // transformation here
    val carNamesDF: DataFrame = carsDS.select(col("Name"))
    // collection transformation maintain types info
    val carNamesAlt: Dataset[String] = carsDS.map(_.Name)
    carNamesAlt.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }
  /*
  * Exercises
  * 1 - Count how many powerful cars we have in the DS (HP > 140)
  * 2 - Avg HP for the entire DS
  * 3 - Count the cars by the origin*/

  def exe1() = {
    val carsDS = readCars()
    carsDS.filter(_.Horsepower.getOrElse(0L) > 140)
      .writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def exe2() = {
    val carsDS = readCars()
    carsDS.select(avg(col("Horsepower")))
      .writeStream
      .format("console")
      .outputMode("complete") // append mode not supported
      .start()
      .awaitTermination()
  }

  def exe3() = {
    val carsDS = readCars()
    val carsCountByOrigin = carsDS.groupBy(col("Origin")).count()
    val carsCountByOriginAlt = carsDS.groupByKey(_.Origin).count()
    carsCountByOrigin.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    showCarsNames()
  }
}
