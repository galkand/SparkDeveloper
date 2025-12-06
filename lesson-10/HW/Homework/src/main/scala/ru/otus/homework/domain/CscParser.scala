package ru.otus.homework.domain

/**
 * Type-класс для преобразования строки CSV (массив полей) в объект типа T.
 */
trait CsvParser[T] extends Serializable {
  def fromRow(fields: Array[String]): T
}
