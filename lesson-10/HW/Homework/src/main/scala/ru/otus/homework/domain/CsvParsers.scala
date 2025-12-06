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
        pickupLongitude      = f(5).toDouble,
        pickupLatitude       = f(6).toDouble,
        rateCodeId           = f(7).toInt,
        storeAndFwdFlag      = f(8),
        dropoffLongitude     = f(9).toDouble,
        dropoffLatitude      = f(10).toDouble,
        paymentType          = f(11).toInt,
        fareAmount           = f(12).toDouble,
        extra                = f(13).toDouble,
        mtaTax               = f(14).toDouble,
        tipAmount            = f(15).toDouble,
        tollsAmount          = f(16).toDouble,
        improvementSurcharge = f(17).toDouble,
        totalAmount          = f(18).toDouble
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
