package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result

/**
 * Main entry point for accessing tasks data from the network.
 */
interface NetworkDataSource {

  suspend fun getExchangeRate(currencyCode: CurrencyCode): Result<ExchangeRate, NetworkError>

  suspend fun getCurrencyCodes(): Result<List<CurrencyCode>, NetworkError>
}