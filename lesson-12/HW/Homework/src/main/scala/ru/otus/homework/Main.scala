package ru.otus.homework

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Main {

  

  def writeParquet(df: DataFrame, outputPath: String): Unit = {
    df.write.mode("overwrite").parquet(outputPath)
  }


  def main(args: Array[String]): Unit = {

    val taxiZonesInputPath = "src/main/resources/data/taxi_zones.csv"
    val yellowTaxiInputPath = "src/main/resources/data/yellow_taxi_jan_25_2018.parquet"

    val outTrips = "target/out/dm_zone_x_trip_agg.parquet"
    

    val spark = SparkSession.builder()
      .appName("Homework")
      .master("local[*]")
      .getOrCreate()


    spark.stop()

  }
}
