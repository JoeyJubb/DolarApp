package com.joe.dolarApp.domain

import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result

interface ConversionRepository {

  /**
   * Attempts to get an [ExchangeRate] for the given [CurrencyCode]
   *
   * @param forceRefresh when true, ignore any local cache
   */
  suspend fun getExchangeRate(
    currencyCode: CurrencyCode,
    forceRefresh: Boolean = false,
  ): Result<ExchangeRate, NetworkError>

  suspend fun getAvailableCurrencies(): Result<List<CurrencyCode>, NetworkError>
}
