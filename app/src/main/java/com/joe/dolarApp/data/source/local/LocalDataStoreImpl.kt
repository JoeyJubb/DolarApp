package com.joe.dolarApp.data.source.local

import com.joe.dolarApp.domain.Conversion
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.coRunCatching
import dagger.Reusable
import jakarta.inject.Inject
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.flatMap

@Reusable
class LocalDataStoreImpl @Inject constructor(
  private val exchangeRateDao: ExchangeRateDao,
): LocalDataStore {

  override suspend fun upsert(exchangeRate: ExchangeRate): Result.Basic<Unit> = coRunCatching {
    exchangeRateDao.upsert(toDataModel(exchangeRate))
  }

  private fun toDataModel(domain: ExchangeRate) =  LocalExchangeRate(
    currencyCode = domain.currencyCode.value,
    askTenDecimals = domain.ask.tenDecimalPlaces,
    bidTenDecimals = domain.bid.tenDecimalPlaces,
    timeStamp = domain.timeStamp,
  )

  override suspend fun get(currency: CurrencyCode): Result<ExchangeRate, LocalDataStore.NotFoundFailure> = coRunCatching {
    exchangeRateDao.getByCurrencyCode(currency.value)
  }.flatMap{ localExchangeRate ->
    localExchangeRate
      ?.let(::toDomainModel)
      ?.asSuccess()
      ?: Result.ExpectedFailure(LocalDataStore.NotFoundFailure)
  }

  private fun toDomainModel(local: LocalExchangeRate) = ExchangeRate(
    currencyCode = CurrencyCode(local.currencyCode),
    ask = Conversion(local.askTenDecimals),
    bid = Conversion(local.bidTenDecimals),
    timeStamp = local.timeStamp
  )

}