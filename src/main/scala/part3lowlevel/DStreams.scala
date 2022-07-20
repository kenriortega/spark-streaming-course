package part3lowlevel

import common.Stock
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.io.{File, FileWriter}
import java.text.SimpleDateFormat
import java.sql.Date

object DStreams {
  val spark: SparkSession = SparkSession.builder()
    .appName("DStreams")
    .config("spark.master", "local[2]")
    .getOrCreate()

  /*
  * Spark streaming Context = entry point to the DStream API
  * - needs the spark context
  * - a duration = batch interval*/
  val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

  /* DStream
  * - input sources
  * - transformations
  *   - call an action
  * - start computation with ssc.start
  *   - no more computation can be added
  * - await termination or stop the computation
  *   - you can restart a computation
  * */

  def readFromSocket() = {
    val socketStream: DStream[String] = ssc.socketTextStream("localhost", 12345)

    // transformation = lazy
    val wordsStream: DStream[String] = socketStream.flatMap(_.split(" "))

    // action
    wordsStream.saveAsTextFiles("src/main/resources/data/words")


    ssc.start()
    ssc.awaitTermination()
  }

  def createNewFile()={
    new Thread(()=>{
      Thread.sleep(5000)
      val path ="src/main/resources/data/stocks"
      val dir = new File(path)
      val nFiles = dir.listFiles().length
      val newFile = new File(s"$path/newStock$nFiles.csv")
      newFile.createNewFile()
      val writer = new FileWriter(newFile)
      writer.write(
        """
          |AAPL,May 1 2000,21
          |AAPL,Jun 1 2000,26.19
          |AAPL,Jul 1 2000,25.41
          |AAPL,Aug 1 2000,30.47
          |AAPL,Sep 1 2000,12.88
          |AAPL,Oct 1 2000,9.78
          |AAPL,Nov 1 2000,8.25
          |AAPL,Dec 1 2000,7.44
          |AAPL,Jan 1 2001,10.81
          |AAPL,Feb 1 2001,9.12
          |""".stripMargin.trim
      )
      writer.close()

    }).start()
  }
  def readFromFile() = {
    createNewFile()//operate on another thread
    val stocksFilePath = "src/main/resources/data/stocks"
    /*ssc.textFileStream monitor a directory for a new files*/
    val textStream: DStream[String] = ssc.textFileStream(stocksFilePath)

    // transformations
    val dateFormat = new SimpleDateFormat("MMM d yyyy")
    val stocksStream: DStream[Stock] = textStream.map { line =>
      val tokens = line.split(",")
      val company = tokens(0)
      val date = new Date(dateFormat.parse(tokens(1)).getTime)
      val price = tokens(2).toDouble
      Stock(company, date, price)
    }

    // action
    stocksStream.print()
    // start computation
    ssc.start()
    ssc.awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    readFromFile()
  }
}
