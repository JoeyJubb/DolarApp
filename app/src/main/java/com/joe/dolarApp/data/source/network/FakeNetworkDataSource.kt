
package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asResult
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.coTryCatching
import com.joe.dolarApp.util.errorHandling.mapError
import dagger.Reusable
import kotlinx.coroutines.time.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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
class FakeNetworkDataSource @Inject constructor() : NetworkDataSource {

  private val exchangeRates : List<ExchangeRate> by lazy {
    listOf(
    ExchangeRate(
      foreign = CurrencyCode("MXN"),
      domestic = CurrencyCode("USDc"),
      ask = "1.83119000000",
      bid = "1.82819000000",
      timeStamp = LocalDateTime.parse("2025-11-29T13:46:21.477342420").toInstant(TimeZone.UTC)
    ),
    ExchangeRate(
      foreign = CurrencyCode("ARS"),
      domestic = CurrencyCode("USDc"),
      ask = "1.5115100000000",
      bid = "1.4865543000000",
      timeStamp = LocalDateTime.parse("2025-11-29T13:46:21.486365910").toInstant(TimeZone.UTC)
    ),
    ExchangeRate(
      foreign = CurrencyCode("BRL"),
      domestic = CurrencyCode("USDc"),
      ask = "5.3822775000",
      bid = "5.3256380000",
      timeStamp = LocalDateTime.parse("2025-11-29T13:46:21.494420614").toInstant(TimeZone.UTC)
    ),
    ExchangeRate(
      foreign = CurrencyCode("COP"),
      domestic = CurrencyCode("USDc"),
      ask = "3.7876313000000",
      bid = "3.7466300000000",
      timeStamp = LocalDateTime.parse("2025-11-29T13:46:21.502238239").toInstant(TimeZone.UTC)
    ),
  )
  }

  override suspend fun getExchangeRate(
    domestic: CurrencyCode,
    foreign: CurrencyCode
  ): Result<ExchangeRate, NetworkError> {
    kotlinx.coroutines.delay(1000L)
    return coTryCatching {
      exchangeRates.find { it.domestic == domestic && it.foreign == foreign }!!
    }
      .mapError { NetworkError.ClientFailure(it) }
  }

  override suspend fun getCurrencyCodes(domestic: CurrencyCode): Result<List<CurrencyCode>, NetworkError> {
    kotlinx.coroutines.delay(1000L)
    return coTryCatching {
      exchangeRates
        .asSequence()
        .filter { it.domestic == domestic }
        .map { it.foreign }
        .toList()
    }
      .mapError { NetworkError.ClientFailure(it) }
  }

}
