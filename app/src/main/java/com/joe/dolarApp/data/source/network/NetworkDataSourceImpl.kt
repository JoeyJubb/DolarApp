package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.NetworkError.ClientFailure
import com.joe.dolarApp.util.errorHandling.NetworkError.NetworkFailure
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.coTryCatching
import com.joe.dolarApp.util.errorHandling.flatMap
import com.joe.dolarApp.util.errorHandling.map
import com.joe.dolarApp.util.errorHandling.mapError
import com.joe.dolarApp.util.errorHandling.tryCatching
import dagger.Reusable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

@Reusable
class NetworkDataSourceImpl @Inject constructor(
  private val dolarAppService: DolarAppService,
) : NetworkDataSource {

  private suspend fun <T> safeCall(
    function: suspend () -> T
  ): Result<T, NetworkError> = coTryCatching {
    function()
  }.mapError { throwable ->
    when (throwable) {
      is HttpException -> NetworkFailure(
        code = throwable.code(),
        cause = throwable,
        debugMessage = throwable.message()
      )
      is UnknownHostException -> NetworkError.Disconnected
      else -> ClientFailure(throwable)
    }
  }

  override suspend fun getExchangeRate(
    domestic: CurrencyCode,
    foreign: CurrencyCode
  ): Result<ExchangeRate, NetworkError> = safeCall {
    dolarAppService.getExchangeRates(foreign.value)
  }.map { it.first() }
    .flatMap(::toDomain)

  private fun toDomain(network: DolarAppService.NetworkExchangeRate): Result<ExchangeRate, NetworkError> =
    tryCatching {
      val (domestic, foreign) = network.book
        .split("_", limit = 2)
        .map { CurrencyCode(it.uppercase()) }

      ExchangeRate(
        domestic = domestic,
        foreign = foreign,
        ask = network.ask,
        bid = network.bid,
        timeStamp = LocalDateTime.parse(network.date).toInstant(TimeZone.UTC)
      )
    }.mapError {
      ClientFailure(it)
    }

  override suspend fun getCurrencyCodes(domestic: CurrencyCode): Result<List<CurrencyCode>, NetworkError> = safeCall {
    dolarAppService.getCurrencies()
  }.map {
    it.map { currency ->
      CurrencyCode(currency)
    }
  }


}