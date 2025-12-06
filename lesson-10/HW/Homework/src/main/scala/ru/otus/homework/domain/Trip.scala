package ru.otus.homework.domain

final case class Trip(
  vendorId: Int,
  pickupDatetime: String,
  dropoffDatetime: String,
  passengerCount: Int,
  tripDistance: Double,
  pickupLongitude: Double,
  pickupLatitude: Double,
  rateCodeId: Int,
  storeAndFwdFlag: String,
  dropoffLongitude: Double,
  dropoffLatitude: Double,
  paymentType: Int,
  fareAmount: Double,
  extra: Double,
  mtaTax: Double,
  tipAmount: Double,
  tollsAmount: Double,
  improvementSurcharge: Double,
  totalAmount: Double
)
