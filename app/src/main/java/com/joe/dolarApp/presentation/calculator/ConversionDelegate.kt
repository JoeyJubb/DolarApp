package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ConversionMode
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.flatMap
import com.joe.dolarApp.util.errorHandling.getOrDefault
import com.joe.dolarApp.util.errorHandling.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface ConversionDelegate {

  fun observe(): Flow<Result<CalculatorUiState.ConversionUiState, Unit>>

  /**
   * swap between BID and ASK
   */
  suspend fun flip()

  suspend fun setExchangeRate(exchangeRate: ExchangeRate)

  suspend fun onDomesticUpdated(text: String)
  suspend fun onForeignUpdated(text: String)

}

class ConversionDelegateImpl @Inject constructor(
  private val currencyExchanger: CurrencyExchanger,
  private val formatProvider: CurrencyFormatterProvider,
) : ConversionDelegate {

  private data class TextHolder(
    val domestic: String,
    val foreign: String,
  )

  private val mode = MutableStateFlow(ConversionMode.BID)
  private val exchangeRate = MutableStateFlow<ExchangeRate?>(null)
  private val exchangeError = MutableStateFlow(false)
  private val text = MutableStateFlow<TextHolder?>(null)

  override fun observe(): Flow<Result<CalculatorUiState.ConversionUiState, Unit>> = combine(
    mode,
    exchangeRate.filterNotNull(),
    exchangeError,
    text.filterNotNull(),
  ){ mode, exchangeRate, exchangeError, text ->

    CalculatorUiState.ConversionUiState(
      mode = mode,
      exchangeRate = exchangeRate,
      domestic = CalculatorUiState.TextState(
        value = text.domestic,
        format = { formatProvider.format(it, exchangeRate.domestic )},
      ),
      foreign = CalculatorUiState.TextState(
        value = text.foreign,
        format = { formatProvider.format(it, exchangeRate.foreign )},
      ),
      inputError = exchangeError,
    ).asSuccess()
  }

  private fun CurrencyFormatterProvider.format(string: String, currencyCode: CurrencyCode): String =
    get(currencyCode).flatMap { it.format(string) }.getOrDefault(string)

  override suspend fun flip() = mode.update {
    when (it) {
      ConversionMode.ASK -> ConversionMode.BID
      ConversionMode.BID -> ConversionMode.ASK
    }
  }

  override suspend fun setExchangeRate(exchangeRate: ExchangeRate) {
    this.exchangeRate.update {
      exchangeRate
    }
    this.text.update { prev ->
      prev ?: TextHolder(
        domestic = "1",
        foreign = exchangeRate.rate
      )
    }
  }

  override suspend fun onDomesticUpdated(text: String) = this.text.update {
    TextHolder(
      domestic = text,
      foreign = calculateOther(text = text, invert = mode.value == ConversionMode.ASK)
    )
  }

  override suspend fun onForeignUpdated(text: String) = this.text.update {
    TextHolder(
      domestic = calculateOther(text = text, invert = mode.value == ConversionMode.BID),
      foreign = text
    )
  }

  private val ExchangeRate.rate : String get() =
    when (mode.value) {
      ConversionMode.ASK -> ask
      ConversionMode.BID -> bid
    }

  private fun getCurrentRate(): String = exchangeRate.value?.rate ?: "1"

  private suspend fun calculateOther(
    text: String,
    invert: Boolean,
  ): String {
    exchangeError.update { false }
    return currencyExchanger.doExchange(
      value = text,
      rate = getCurrentRate(),
      invertRate = invert
    ).onFailure {
      exchangeError.update { true }
    }.getOrDefault("0")
  }


}
