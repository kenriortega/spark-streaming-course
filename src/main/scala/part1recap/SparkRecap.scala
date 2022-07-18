package part1recap

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, col, expr}

object SparkRecap extends App {

  // the entry point to the spark structured API
  val spark = SparkSession.builder()
    .appName("SparkRecap")
    .config("spark.master", "local")
    .getOrCreate()

  // read a DF
  val cars = spark.read
    .option("inferSchema", "true")
    .json("src/main/resources/data/cars/cars.json")

  cars.show

  import spark.implicits._

  val usefulCarsData = cars.select(
    col("Name"),
    $"Year", // another column object need (spark.implicits._)
    (col("Weight_in_lbs") / 2.2).as("Weight_in_kg"),
    expr("Weight_in_lbs / 2.2").as("Weight_in_kg_2")
  )

  val carsWeights = cars.selectExpr("Weight_in_lbs / 2.2")

  // filtering
  val europeanCars = cars.filter(col("Origin") =!= "USA")
  // val aggregations
  val avgHP = cars.select(avg(col("Horsepower")) as "avg_hp") // sum, mean, min, max
  // grouping
  val countByOrigin = cars
    .groupBy(col("Origin")) // a relational grouped datasets
    .count()
  // joining
  val guitarPlayers = spark.read
    .option("inferSchema", "true")
    .json("src/main/resources/data/guitarPlayers")
  val bands = spark.read
    .option("inferSchema", "true")
    .json("src/main/resources/data/bands")

  val guitaristsBands = guitarPlayers
    .join(
      bands,
      guitarPlayers.col("band") === bands.col("id")
    )
  /*
  * join types
  * - inner : only the matching rows are kept
  * - left/right/full outer join
  * - semi/anti
  * */

  // Datasets  = typed distributed collection of objects
  case class GuitarPlayer(id: Long, name: String, guitars: Seq[Long], band: Long)

  val guitarPlayersDS = guitarPlayers.as[GuitarPlayer] // spark implicits
  guitarPlayersDS.map(_.name)

  // type safety benefits of Datasets

  // Spark SQL
  cars.createOrReplaceTempView("cars")
  val americanCars = spark.sql(
    """
      |select Name from cars where Origin = 'USA'
      |""".stripMargin
  )

  // low-level API: RDDs
  val sc = spark.sparkContext
  val numbersRDD = sc.parallelize(1 to 1000000)
  // functional operators
  val doubles = numbersRDD.map(_ * 2)

  // RDD -> DF
  val numbersDF = numbersRDD.toDF("number") // you lose type info, you get sql capability
  // RDD -> DS
  val numbersDS = spark.createDataset(numbersRDD)
  // DS -> RDD
  val guitarPlayersRDD = guitarPlayersDS.rdd
  // DF -> RDD
  val carsRDD = cars.rdd
}
