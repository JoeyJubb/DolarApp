package com.joe.dolarApp.data.source.network

import retrofit2.http.GET
import retrofit2.http.Query

interface DolarAppService {

  @GET("tickers-currencies")
  suspend fun getCurrencies(): List<String>


  @GET("tickers")
  suspend fun getExchangeRates(@Query("currencies") vararg currencyCode: String): List<NetworkExchangeRate>

  data class NetworkExchangeRate(
    val ask: String,
    val bid: String,
    val book: String,
    val date: String,
  )

}