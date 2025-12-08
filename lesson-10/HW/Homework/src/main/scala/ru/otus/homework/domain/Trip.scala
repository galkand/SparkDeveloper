package ru.otus.homework.domain

final case class Trip(
  vendorId: Int,
  pickupDatetime: String,
  dropoffDatetime: String,
  passengerCount: Int,
  tripDistance: Double,
  rateCodeId: Int,
  storeAndFwdFlag: String,
  puLocationId: Int,
  doLocationId: Int,
  paymentType: Int,
  fareAmount: Double,
  extra: Double,
  mtaTax: Double,
  tipAmount: Double,
  tollsAmount: Double,
  improvementSurcharge: Double,
  totalAmount: Double,
  congestionSurcharge: Double,
  airportFee: Double
)

