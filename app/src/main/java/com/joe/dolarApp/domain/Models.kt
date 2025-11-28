package com.joe.dolarApp.domain

import kotlinx.datetime.LocalDateTime

@JvmInline
value class CurrencyCode(val value: String)

data class ExchangeRate(
  val currencyCode: CurrencyCode,
  val ask: Conversion,
  val bid: Conversion,
  val timeStamp: LocalDateTime
)

/**
 * Conversion rate is stored as in [Long] with a precision of ten decimal places
 *
 * @param tenDecimalPlaces conversion rate multiplied by 10 billion (10,000,000,000)
 */
data class Conversion(
  val tenDecimalPlaces: Long,
)
