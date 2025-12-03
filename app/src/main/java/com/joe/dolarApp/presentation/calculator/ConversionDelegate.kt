package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.R
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ConversionMode
import com.joe.dolarApp.presentation.common.ResourceProvider
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

  fun observe(): Flow<CalculatorUiState.ConversionUiState>

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
  private val resourceProvider: ResourceProvider,
) : ConversionDelegate {

  private data class TextHolder(
    val domestic: String = "0",
    val foreign: String = "0",
  )

  private val mode = MutableStateFlow(ConversionMode.BID)
  private val exchangeRate = MutableStateFlow<ExchangeRate?>(null)
  private val exchangeError = MutableStateFlow(false)
  private val text = MutableStateFlow<TextHolder>(TextHolder())

  override fun observe(): Flow<CalculatorUiState.ConversionUiState> = combine(
    mode,
    exchangeRate.filterNotNull(),
    exchangeError,
    text,
  ) { mode, exchangeRate, exchangeError, text ->
    CalculatorUiState.ConversionUiState(
      mode = mode,
      exchangeRate = exchangeRate,
      domestic = format(text.domestic, exchangeRate.domestic),
      foreign = format(text.foreign, exchangeRate.foreign),
      inputError = exchangeError,
      exchangeRateString = resourceProvider.getString(
        R.string.conversion_string,
        format("1", exchangeRate.domestic, false),
        format(getCurrentRate(), exchangeRate.foreign, false),
      )
    )
  }

  private fun format(
    string: String,
    currencyCode: CurrencyCode,
    roundToDecimals: Boolean = true,
  ): String =
    formatProvider[currencyCode]
      .flatMap { it.toCurrencyString(string, roundToDecimals) }
      .getOrDefault(string)

  private fun parseTypedInput(string: String, code: CurrencyCode): String =
    formatProvider[code]
      .flatMap { it.parseTypedInput(string) }
      .getOrDefault(string)

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
  }

  override suspend fun onDomesticUpdated(text: String) {
    exchangeRate.value?.let { exchangeRate ->
      this.text.update {
        val parsed = parseTypedInput(text, exchangeRate.domestic)
        TextHolder(
          domestic = parsed,
          foreign = calculateOther(
            text = parsed,
            invert = mode.value == ConversionMode.ASK
          )
        )
      }
    }
  }

  override suspend fun onForeignUpdated(text: String) {
    exchangeRate.value?.let { exchangeRate ->
      this.text.update {
        val parsed = parseTypedInput(text, exchangeRate.foreign)
        TextHolder(
          foreign = parsed,
          domestic = calculateOther(
            text = parsed,
            invert = mode.value == ConversionMode.BID
          )
        )
      }
    }
  }

  private val ExchangeRate.rate: String
    get() =
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
    }
      .getOrDefault("")
  }


}
