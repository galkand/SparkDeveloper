package ru.otus.homework

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Main {

  def readCountries(spark: SparkSession, inputPath: String): DataFrame = {

  val schema = StructType(Seq(
    StructField("cca3", StringType, nullable = true),

    StructField("name",
      StructType(Seq(
        StructField("common", StringType, nullable = true)
      )),
      nullable = true
    ),

    StructField("borders", ArrayType(StringType, containsNull = true), nullable = true),

    // Ключевой момент: languages как MAP, а не STRUCT
    StructField("languages", MapType(StringType, StringType, valueContainsNull = true), nullable = true)
  ))

  spark.read
    .schema(schema)
    .option("multiline", "true")
    // если вдруг попадётся "грязный" JSON — лучше не падать, а занулить
    .option("mode", "PERMISSIVE")
    .json(inputPath)
}

  def countriesWith5PlusBorders(countriesDf: DataFrame): DataFrame = {
    // справочник: cca3 -> name.common
    val countryRef = countriesDf
      .select(
        col("cca3").as("cca3_key"),
        col("name.common").as("country_name")
      )
      .where(col("cca3_key").isNotNull && col("country_name").isNotNull)

    val normalized = countriesDf
      .select(
        col("cca3").as("cca3"),
        col("name.common").as("Country"),
        // borders может быть null => приводим к пустому массиву
        coalesce(col("borders").cast(ArrayType(StringType)), array().cast(ArrayType(StringType))).as("borders")
      )
      .where(col("cca3").isNotNull && col("Country").isNotNull)

    normalized
      .withColumn("border_cca3", explode_outer(col("borders")))
      .join(countryRef, col("border_cca3") === col("cca3_key"), "left")
      .groupBy(col("cca3"), col("Country"))
      .agg(
        first(size(col("borders"))).as("NumBorders"),
        array_sort(array_distinct(collect_list(col("country_name")))).as("BorderCountriesArr")
      )
      // убираем null, если какие-то cca3 не нашли в справочнике
      .withColumn("BorderCountriesArr", expr("filter(BorderCountriesArr, x -> x is not null)"))
      .withColumn("BorderCountries", concat_ws(", ", col("BorderCountriesArr")))
      .drop("BorderCountriesArr", "cca3")
      .where(col("NumBorders") >= 5)
      .select(col("Country"), col("NumBorders"), col("BorderCountries"))
      .orderBy(col("NumBorders").desc, col("Country").asc)
  }

  def languageRanking(countriesDf: DataFrame): DataFrame = {
    val exploded = countriesDf
      .select(
        col("name.common").as("Country"),
        map_values(col("languages")).as("Languages")
      )
      .where(col("Country").isNotNull)
      .withColumn("Language", explode_outer(col("Languages")))
      .where(col("Language").isNotNull)

    exploded
      .groupBy(col("Language"))
      .agg(
        countDistinct(col("Country")).as("NumCountries"),
        array_sort(array_distinct(collect_list(col("Country")))).as("Countries")
      )
      .orderBy(col("NumCountries").desc, col("Language").asc)
  }

  def writeParquet(df: DataFrame, outputPath: String): Unit = {
    df.write.mode("overwrite").parquet(outputPath)
  }


  def main(args: Array[String]): Unit = {

    val inputPath = "src/main/resources/data/countries.json"

    val outBorders = "target/out/countries_5plus_borders.parquet"
    val outLangs   = "target/out/language_ranking.parquet"

    val spark = SparkSession.builder()
      .appName("Homework")
      .master("local[*]")
      .getOrCreate()

    val countriesDf = readCountries(spark, inputPath).cache()
    val dfBorders = countriesWith5PlusBorders(countriesDf)
    val dfLangs   = languageRanking(countriesDf)

    writeParquet(dfBorders, outBorders)
    writeParquet(dfLangs, outLangs)

    dfBorders.show(20, truncate = false)
    dfLangs.show(20, truncate = false)

    spark.stop()
  }
}
