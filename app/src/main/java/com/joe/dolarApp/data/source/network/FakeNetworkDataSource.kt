
package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.Conversion
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asResult
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.mapError
import dagger.Reusable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

/**
 * Temporary implementation during development.
 *
 * # Advantages of temp implementations
 *
 * ### Dev Testing
 * Much easier to dev test along the way because we're in complete control of what is returned.
 * We can test how the UI responds to a very slow load, all the types of failure, and the happy
 * path.
 *
 * ### Give the BE some time
 * It's not uncommon that the BE team are developing their implementation in tandem with front end.
 * Once the API is agreed upon, FE can get on with implementation and not be blocked waiting for BE
 * to finish completely
 *
 * ### Unit testing other classes
 * We're building the fake implementation now! We can use it in unit tests straight away, and when
 * we're ready to use a proper implementation we can move this class to the test sources.
 *
 * # Unit Testing temporary implementations
 * It's not necessary to write unit tests for this as we're never going to ship it. Every new feature
 * should be gated behind some network-side switch (often called a feature flag).
 *
 * If the CI requires a level of coverage (as it should), then we can get an exemption for this
 * class only until we're ready to integrate properly into the backend.
 */
@Reusable
class FakeNetworkDataSource @Inject constructor(
  private val exchangeRates : List<ExchangeRate> = listOf(
    ExchangeRate(
      currencyCode = CurrencyCode("MXN"),
      ask = Conversion(183119000000L),
      bid = Conversion(182819000000L),
      timeStamp = Instant.parse("2025-11-29T13:46:21.477342420")
    ),
    ExchangeRate(
      currencyCode = CurrencyCode("ARS"),
      ask = Conversion(15115100000000L),
      bid = Conversion(14865543000000L),
      timeStamp = Instant.parse("2025-11-29T13:46:21.486365910")
    ),
    ExchangeRate(
      currencyCode = CurrencyCode("BRL"),
      ask = Conversion(53822775000L),
      bid = Conversion(53256380000L),
      timeStamp = Instant.parse("2025-11-29T13:46:21.494420614")
    ),
    ExchangeRate(
      currencyCode = CurrencyCode("COP"),
      ask = Conversion(37876313000000L),
      bid = Conversion(37466300000000L),
      timeStamp = Instant.parse("2025-11-29T13:46:21.502238239")
    ),
  )
) : NetworkDataSource {


  override suspend fun getExchangeRate(
    currencyCode: CurrencyCode
  ): Result<ExchangeRate, NetworkError> = exchangeRates
    .find { it.currencyCode == currencyCode }
    .asResult()
    .mapError{ NetworkError.NetworkFailure(debugMessage = "Cannot find exchange rate for $currencyCode") }

  override suspend fun getCurrencyCodes(): Result<List<CurrencyCode>, NetworkError> = exchangeRates
    .map { it.currencyCode }
    .asSuccess()
}
