package com.joe.dolarApp.data.source.local

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.Result

interface LocalDataStore {

  suspend fun upsert(exchangeRate: ExchangeRate) : Result.Basic<Unit>


  data object NotFoundFailure
  suspend fun get(currency: CurrencyCode): Result<ExchangeRate, NotFoundFailure>
}