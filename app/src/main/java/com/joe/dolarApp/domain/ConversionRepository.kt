package com.joe.dolarApp.domain

import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result

interface ConversionRepository {

  /**
   * Attempts to get an [ExchangeRate] for the given [domestic] and [foreign] currency codes
   *
   * @param forceRefresh when true, ignore any local cache
   */
  suspend fun getExchangeRate(
    domestic: CurrencyCode,
    foreign: CurrencyCode,
    forceRefresh: Boolean = false,
  ): Result<ExchangeRate, NetworkError>

  /**
   * Get a list of foreign currencies we can get exchange rates for the given [domestic] currency
   */
  suspend fun getAvailableForeignCodes(
    domestic: CurrencyCode,
  ): Result<List<CurrencyCode>, NetworkError>
}
