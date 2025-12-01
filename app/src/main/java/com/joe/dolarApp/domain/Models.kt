package com.joe.dolarApp.domain

import kotlinx.datetime.Instant

@JvmInline
value class CurrencyCode(val value: String)

data class ExchangeRate(
  val domestic: CurrencyCode,
  val foreign: CurrencyCode,
  val ask: String,
  val bid: String,
  val timeStamp: Instant
)
