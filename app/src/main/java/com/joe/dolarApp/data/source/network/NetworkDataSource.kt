package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate

/**
 * Main entry point for accessing tasks data from the network.
 */
interface NetworkDataSource {

    suspend fun getExchangeRate(currencyCode: CurrencyCode): Result<ExchangeRate>

    suspend fun getCurrencyCodes() : Result<List<CurrencyCode>>
}