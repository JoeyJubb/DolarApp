
package com.joe.dolarApp.data.source.network

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import dagger.Reusable
import javax.inject.Inject

@Reusable
class NetworkDataSourceImpl @Inject constructor(

) : NetworkDataSource {

  override suspend fun getExchangeRate(
    currencyCode: CurrencyCode
  ): Result<ExchangeRate, NetworkError> = TODO()

  override suspend fun getCurrencyCodes(

  ): Result<List<CurrencyCode>, NetworkError> = TODO()
}
