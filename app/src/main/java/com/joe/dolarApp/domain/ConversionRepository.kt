package com.joe.dolarApp.domain

import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result

interface ConversionRepository {

  suspend fun getExchangeRate(
    currency: CurrencyCode,
    forceRefresh: Boolean = false,
  ): Result<ExchangeRate, NetworkError>

  suspend fun getAvailableCurrencies(
    forceRefresh: Boolean = false,
  ): Result<List<CurrencyCode>, NetworkError>
}
