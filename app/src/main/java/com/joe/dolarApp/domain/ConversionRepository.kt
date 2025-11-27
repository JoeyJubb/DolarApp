package com.joe.dolarApp.domain

interface ConversionRepository {

  suspend fun getExchangeRate(
    currency: CurrencyCode,
    forceRefresh: Boolean = false,
  ): Result<ExchangeRate>

  suspend fun getAvailableCurrencies(
    forceRefresh: Boolean = false,
  ): Result<List<CurrencyCode>>
}
