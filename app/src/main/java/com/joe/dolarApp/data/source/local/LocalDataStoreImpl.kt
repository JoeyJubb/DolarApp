package com.joe.dolarApp.data.source.local

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import dagger.Reusable
import jakarta.inject.Inject
import kotlin.Result.Companion.failure

/*
 * The purpose of this class is to wrap the room implementation with something that deals with our
 * domain objects
 */
@Reusable
class LocalDataStoreImpl @Inject constructor(): LocalDataStore {

  override fun upsert(exchangeRate: ExchangeRate): Result<Unit> = failure(NotImplementedError())

  override fun get(currency: CurrencyCode): Result<ExchangeRate> = failure(NotImplementedError())
}