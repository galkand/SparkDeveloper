package ru.otus.homework.domain


object CsvParsers {

  implicit val tripCsvParser: CsvParser[Trip] = new CsvParser[Trip] {
    override def fromRow(f: Array[String]): Trip =
      Trip(
        vendorId             = f(0).toInt,
        pickupDatetime       = f(1),
        dropoffDatetime      = f(2),
        passengerCount       = f(3).toInt,
        tripDistance         = f(4).toDouble,
        rateCodeId           = f(5).toInt,
        storeAndFwdFlag      = f(6),
        puLocationId         = f(7).toInt,
        doLocationId         = f(8).toInt,
        paymentType          = f(9).toInt,
        fareAmount           = f(10).toDouble,
        extra                = f(11).toDouble,
        mtaTax               = f(12).toDouble,
        tipAmount            = f(13).toDouble,
        tollsAmount          = f(14).toDouble,
        improvementSurcharge = f(15).toDouble,
        totalAmount          = f(16).toDouble,
        congestionSurcharge  = f(17).toDouble,
        airportFee           = f(18).toDouble
      )
}


  implicit val taxiZoneCsvParser: CsvParser[TaxiZone] = new CsvParser[TaxiZone] {
    override def fromRow(f: Array[String]): TaxiZone =
      TaxiZone(
        locationId  = f(0).toInt,
        borough     = f(1).replace("\"", ""),
        zone        = f(2).replace("\"", ""),
        serviceZone = f(3).replace("\"", "")
      )
  }
}
