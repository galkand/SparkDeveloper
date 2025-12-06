import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import ru.otus.homework.domain.{CsvParser, Trip, TaxiZone}
import ru.otus.homework.domain.CsvParsers._
import scala.reflect.ClassTag

object Main {

  
  def loadCsv[T: CsvParser : ClassTag](
    path: String,
    delimiter: String = ","
  )(implicit sc: SparkContext): RDD[T] = {
    val parser = implicitly[CsvParser[T]]

    val lines = sc.textFile(path)
    val header = lines.first()
    val data  = lines.filter(_ != header)

    data
      .map(_.split(delimiter, -1))
      .map(parser.fromRow)
  }

  def main(args: Array[String]): Unit = {

    implicit val sc: SparkContext =
      new SparkContext(new SparkConf().setAppName("Homework").setMaster("local[*]"))

    try {
      val trips: RDD[Trip] =
        loadCsv[Trip]("src/main/resources/data/tripdata.csv")

      val zones: RDD[TaxiZone] =
        loadCsv[TaxiZone]("src/main/resources/data/taxi_zone_lookup.csv")

      println("Первые 3 поездки:")
      trips.take(3).foreach(println)

      println("Первые 3 зоны:")
      zones.take(3).foreach(println)
    } finally {
      sc.stop()
    }
  }
}
