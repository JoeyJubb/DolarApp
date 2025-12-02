package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.recover
import javax.inject.Inject

/**
 * At time of writing, "https://api.dolarapp.dev/v1/tickers-currencies" is retuning a 403.
 *
 * In order to demonstrate more than just an error page, I've added this implementation which will
 * fallback to the fake data source if the real one fails.
 */
class DelegatingNetworkDataSource @Inject constructor(
  val actual: NetworkDataSourceImpl,
  val fake: FakeNetworkDataSource,
) : NetworkDataSource {

  /**
   * Always use the real impl for this one
   */
  override suspend fun getExchangeRate(
    domestic: CurrencyCode,
    foreign: CurrencyCode
  ): Result<ExchangeRate, NetworkError> = actual.getExchangeRate(domestic, foreign)

  /**
   * Use the fake implementation if the we're still getting a 403
   */
  override suspend fun getCurrencyCodes(domestic: CurrencyCode): Result<List<CurrencyCode>, NetworkError> =
    withFallback {
      it.getCurrencyCodes(domestic)
    }

  private suspend fun <T> withFallback(
    function: suspend (NetworkDataSource) -> Result<T, NetworkError>
  ): Result<T, NetworkError> {
    return function(actual)
      .recover {
        if(shouldRecover(it)){
          function(fake)
        }else{
          Result.Failure(it)
        }
      }
  }

  private fun shouldRecover(it: NetworkError): Boolean =
    (it as? NetworkError.NetworkFailure)?.code == 403
}