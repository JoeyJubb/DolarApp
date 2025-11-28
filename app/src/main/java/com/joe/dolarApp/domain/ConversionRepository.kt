package com.joe.dolarApp.domain

import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.NetworkError

interface ConversionRepository {

  suspend fun getExchangeRate(
    currency: CurrencyCode,
    forceRefresh: Boolean = false,
  ): Result<ExchangeRate, NetworkError>

  suspend fun getAvailableCurrencies(
    forceRefresh: Boolean = false,
  ): Result<List<CurrencyCode>, NetworkError>
}
