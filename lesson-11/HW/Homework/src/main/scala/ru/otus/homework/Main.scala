import org.apache.spark.{SparkConf, SparkContext}

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
      
      

    } finally {
      sc.stop()
    }
  }
}
