package com.joe.dolarApp.data.source.local

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.Optional
import com.joe.dolarApp.util.errorHandling.Result

interface LocalDataStore {

  suspend fun upsert(exchangeRate: ExchangeRate) : Result<Unit, Unit>

  suspend fun get(currency: CurrencyCode): Optional<ExchangeRate>
}