package com.joe.dolarApp.data.source.local

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.Optional
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asResult
import com.joe.dolarApp.util.errorHandling.asSuccess
import dagger.Reusable
import jakarta.inject.Inject

@Reusable
class LocalDataStoreImpl @Inject constructor(
  private val exchangeRateDao: ExchangeRateDao,
) : LocalDataStore {

  override suspend fun upsert(exchangeRate: ExchangeRate): Result<Unit, Unit> = exchangeRateDao
    .upsert(toDataModel(exchangeRate))
    .asSuccess()

  private fun toDataModel(domain: ExchangeRate) = LocalExchangeRate(
    currencyCode = domain.currencyCode.value,
    ask = domain.ask,
    bid = domain.bid,
    timeStamp = domain.timeStamp,
  )

  override suspend fun get(currency: CurrencyCode): Optional<ExchangeRate> =
    exchangeRateDao.getByCurrencyCode(currency.value)
      ?.let(::toDomainModel)
      .asResult()

  private fun toDomainModel(local: LocalExchangeRate) = ExchangeRate(
    currencyCode = CurrencyCode(local.currencyCode),
    ask = local.ask,
    bid = local.bid,
    timeStamp = local.timeStamp
  )

}