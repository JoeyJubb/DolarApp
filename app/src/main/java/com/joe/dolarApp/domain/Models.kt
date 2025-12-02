package com.joe.dolarApp.domain

import kotlinx.datetime.Instant

@JvmInline
value class CurrencyCode(val value: String)

/**
 * @param bid the rate you get when selling [domestic] to buy [foreign]
 * @param ask the rate you get when selling [foreign] to buy [domestic]
 */
data class ExchangeRate(
  val domestic: CurrencyCode,
  val foreign: CurrencyCode,
  val ask: String,
  val bid: String,
  val timeStamp: Instant
)
