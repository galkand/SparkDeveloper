package ru.otus.homework

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Main {

  final case class TaxiZone(
    LocationID: Int,
    Borough: String,
    Zone: String,
    service_zone: String
  )

  final case class Trip(
    PULocationID: Int,
    trip_distance: Double
  )

  final case class ZoneTripAgg(
    Zone: String,
    trips_cnt: Long,
    min_distance: Double,
    avg_distance: Double,
    max_distance: Double,
    stddev_distance: Double
)

  def writeParquet(df: DataFrame, outputPath: String): Unit =
    df.write.mode("overwrite").parquet(outputPath)

  def main(args: Array[String]): Unit = {

    val taxiZonesInputPath = "data/taxi_zones.csv"                 
    val yellowTaxiTripInfoInputPath = "data/yellow_taxi_jan_25_2018"
    val dmOutputPath = "target/out/dm_zone_x_trip_agg.parquet"

    val spark = SparkSession.builder()
      .appName("Homework")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val zonesSchema = StructType(Seq(
      StructField("LocationID", IntegerType, nullable = false),
      StructField("Borough", StringType, nullable = true),
      StructField("Zone", StringType, nullable = true),
      StructField("service_zone", StringType, nullable = true)
    ))

    val dsZones: Dataset[TaxiZone] =
      spark.read
        .option("header", "true")
        .option("mode", "FAILFAST")
        .schema(zonesSchema)
        .csv(taxiZonesInputPath)
        .as[TaxiZone]

    val dsTrips: Dataset[Trip] =
      spark.read
        .parquet(yellowTaxiTripInfoInputPath)
        .select(
          col("PULocationID").cast(IntegerType).as("PULocationID"),
          col("trip_distance").cast(DoubleType).as("trip_distance")
        )
        .where(col("PULocationID").isNotNull)
        .where(col("trip_distance").isNotNull && !isnan(col("trip_distance")))
        .where(col("trip_distance") >= lit(0.0))
        .as[Trip]

    dsTrips.toDF().createOrReplaceTempView("trips")
    dsZones.toDF().createOrReplaceTempView("zones")

    val dmSql =
      """
        |SELECT
        |  z.Zone                                    AS Zone,
        |  COUNT(*)                                  AS trips_cnt,
        |  MIN(t.trip_distance)                       AS min_distance,
        |  AVG(t.trip_distance)                       AS avg_distance,
        |  MAX(t.trip_distance)                       AS max_distance,
        |  COALESCE(stddev_samp(t.trip_distance), 0)  AS stddev_distance
        |FROM trips t
        |JOIN zones z
        |  ON z.LocationID = t.PULocationID
        |GROUP BY z.Zone
        |ORDER BY trips_cnt DESC
        |""".stripMargin

    val dmDf = spark.sql(dmSql)

    val dmDs: Dataset[ZoneTripAgg] = dmDf.as[ZoneTripAgg]

    writeParquet(dmDs.toDF(), dmOutputPath)

    dmDs.show(20, truncate = false)
    println(s"Written: $dmOutputPath")

    spark.stop()
  }
}