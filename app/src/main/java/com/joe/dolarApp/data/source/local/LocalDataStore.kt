package com.joe.dolarApp.data.source.local

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate

interface LocalDataStore {
  fun upsert(exchangeRate: ExchangeRate) : Result<Unit>

  fun get(currency: CurrencyCode): Result<ExchangeRate>
}