
package com.joe.dolarApp.domain

import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ConversionRepositoryImpl @Inject constructor(
) : ConversionRepository {

  override suspend fun getExchangeRate(
    currency: CurrencyCode,
    forceRefresh: Boolean
  ): Result<ExchangeRate, NetworkError> = TODO()


  override suspend fun getAvailableCurrencies(forceRefresh: Boolean): Result<List<CurrencyCode>, NetworkError> =
    TODO()

}
