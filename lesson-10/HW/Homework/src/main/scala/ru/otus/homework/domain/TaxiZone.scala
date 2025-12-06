package ru.otus.homework.domain

final case class TaxiZone(
  locationId: Int,
  borough: String,
  zone: String,
  serviceZone: String
)
