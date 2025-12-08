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

      val zonesMap = sc.broadcast(
      zones
        .map(z => z.locationId -> z.borough)
        .collectAsMap()
      )

      val boroughHourCounts: RDD[((String, Int), Long)] =
        trips
          .flatMap { trip =>
            val hour = trip.pickupDatetime.substring(11, 13).toInt
            zonesMap.value.get(trip.puLocationId).map { borough =>
              ((borough, hour), 1L)
            }
          }
          .reduceByKey(_ + _)
          

      val resultLines: RDD[String] =
        boroughHourCounts
          .sortBy({ case ((borough, hour), _) => (borough, hour) })
          .map { case ((borough, hour), count) =>
            s"$borough,$hour,$count"
          } 

      resultLines.saveAsTextFile("output/borough_hour_counts.txt")

    } finally {
      sc.stop()
    }
  }
}
