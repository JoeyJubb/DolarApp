
package com.joe.dolarApp.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExchangeRateDao {

  @Query("SELECT * FROM exchange_rates WHERE currencyCode = :currencyCode")
  suspend fun getByCurrencyCode(currencyCode: String): LocalExchangeRate?

  @Upsert
  suspend fun upsert(exchangeRate: LocalExchangeRate)

  @Query("DELETE FROM exchange_rates")
  suspend fun clear()

}
