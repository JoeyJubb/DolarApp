package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.R
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.CurrencyInputUiState
import com.joe.dolarApp.presentation.common.ResourceProvider
import com.joe.dolarApp.presentation.common.TimeStampFormatter
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.flatMap
import com.joe.dolarApp.util.errorHandling.getOrDefault
import com.joe.dolarApp.util.errorHandling.getOrNull
import com.joe.dolarApp.util.errorHandling.getOrThrow
import com.joe.dolarApp.util.errorHandling.isFailure
import com.joe.dolarApp.util.errorHandling.onFailure
import com.joe.dolarApp.util.errorHandling.onSuccess
import com.joe.dolarApp.util.errorHandling.tryCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import timber.log.Timber
import javax.inject.Inject


/**
 * ViewModel for the task list screen.
 */

interface ConversionDelegate {

  data class State(
    val from: CurrencyInputUiState,
    val to: CurrencyInputUiState,
    val rate: String,
    val timeStamp: String,
  )

  fun observe(): Flow<Result<State, Unit>>

  /**
   * The previous "from" currency becomes the new "to" currency and its value is kept.
   * The previous "to" currency becomes the new "from" currency and its value is recalculated.
   *
   * It's done this way because the designs have "from" at the top and "to" at the bottom, and a
   * down arrow to swap them.
   */
  suspend fun flip()

  suspend fun setExchangeRate(exchangeRate: ExchangeRate)

}

class ConversionDelegateImpl @Inject constructor(
  private val currencyExchanger: CurrencyExchanger,
  private val formatProvider: CurrencyFormatterProvider,
  private val resourceProvider: ResourceProvider,
  private val timeStampFormatter: TimeStampFormatter,
) : ConversionDelegate {

  private data class TextDisplay(
    val text: String = "1",
    val error: Boolean = false,
  )

  private val exchangeRate = MutableStateFlow<ExchangeRate?>(null)
  private val isDomesticToForeign = MutableStateFlow(true)

  private val domesticDisplay = MutableStateFlow(TextDisplay())

  private val foreignDisplay = MutableStateFlow<TextDisplay>(TextDisplay())

  override suspend fun flip() {
    val result = isDomesticToForeign.updateAndGet { !it }
    refresh(!result) // want the top value to transfer to the bottom one
  }

  override suspend fun setExchangeRate(exchangeRate: ExchangeRate) {
    this.exchangeRate.update { exchangeRate }
    refresh(isDomesticToForeign.value)
  }

  override fun observe(): Flow<Result<ConversionDelegate.State, Unit>> =
    combine(
      isDomesticToForeign,
      exchangeRate.filterNotNull(),
      domesticDisplay,
      foreignDisplay,
    ) { isDomesticToForeign, exchangeRate, domesticDisplay, foreignDisplay ->

      val rate = if (isDomesticToForeign) exchangeRate.bid else exchangeRate.ask

      val domestic = CurrencyInputUiState(
        currency = exchangeRate.domestic,
        display = domesticDisplay.text,
        error = domesticDisplay.error,
        showCountryPicker = false,
        onTextChanged = {
          onDomesticChanged(it)
          calculateForeign(
            value = it,
            rate = rate,
            domestic = exchangeRate.domestic
          )
        },
      )

      val foreign = CurrencyInputUiState(
        currency = exchangeRate.foreign,
        display = foreignDisplay.text,
        error = foreignDisplay.error,
        showCountryPicker = true,
        onTextChanged = {
          onForeignChanged(it)
          calculateDomestic(
            value = it,
            rate = rate,
            foreign = exchangeRate.foreign
          )
        },
      )

      val (from, to) = if (isDomesticToForeign) domestic to foreign else foreign to domestic

      ConversionDelegate.State(
        from = from,
        to = to,
        rate = getRateString(isDomesticToForeign, from.currency, to.currency, rate),
        timeStamp = timeStampFormatter(exchangeRate.timeStamp)
          .getOrDefault(exchangeRate.timeStamp.toString()),
      ).asSuccess()
    }

  private fun getRateString(
    isDomesticToForeign: Boolean,
    from: CurrencyCode,
    to: CurrencyCode,
    rate: String
  ): String = tryCatching {

    val fromRate = if (isDomesticToForeign) "1" else rate
    val toRate = if (isDomesticToForeign) rate else "1"

    resourceProvider.getString(
      R.string.conversion_string,
      formatProvider[from].flatMap { it.format(fromRate) }.getOrThrow(),
      formatProvider[to].flatMap { it.format(toRate) }.getOrThrow(),
    )
  }.getOrDefault("1 ${from.value} = $rate ${to.value}")

  private suspend fun calculateForeign(
    value: String,
    rate: String,
    domestic: CurrencyCode
  ) {
    currencyExchanger.doExchange(
      value = value,
      rate = rate,
      from = domestic,
      invertRate = false
    ).onSuccess { result ->
      onForeignChanged(result)
    }.onFailure {
      Timber.e(it)
      domesticDisplay.update { prev -> prev.copy(error = true) }
    }
  }

  private suspend fun calculateDomestic(
    value: String,
    rate: String,
    foreign: CurrencyCode,
  ) {
    currencyExchanger.doExchange(
      value = value,
      rate = rate,
      from = foreign,
      invertRate = true
    ).onSuccess { result ->
      onDomesticChanged(result)
    }.onFailure {
      Timber.e(it)
      foreignDisplay.update { prev -> prev.copy(error = true) }
    }
  }

  private fun onDomesticChanged(string: String) {
    domesticDisplay.update {
      cleanupInput(string) { domestic }
    }
  }

  private fun cleanupInput(
    string: String,
    currencyCode: ExchangeRate.() -> CurrencyCode
  ): TextDisplay = exchangeRate.value?.let {
    formatProvider[currencyCode(it)].flatMap { formatter ->
      formatter.parse(string).flatMap { decimal -> formatter.format(decimal) }
    }.let { result ->
      TextDisplay(
        text = result.getOrNull() ?: string,
        error = result.isFailure(),
      )
    }
  } ?: TextDisplay(string)

  private fun onForeignChanged(string: String) {
    foreignDisplay.update {
      cleanupInput(string) { foreign }
    }
  }

  private suspend fun refresh(
    isDomesticToForeign: Boolean,
  ) {
    exchangeRate.value?.let { exchangeRate ->
      val rate = if (isDomesticToForeign) exchangeRate.bid else exchangeRate.ask

      if (isDomesticToForeign) {
        refreshDomestic()

        calculateForeign(
          value = domesticDisplay.value.text,
          rate = rate,
          domestic = exchangeRate.domestic
        )
      } else {
        refreshForeign()

        calculateDomestic(
          value = foreignDisplay.value.text,
          rate = rate,
          foreign = exchangeRate.foreign
        )
      }
    }
  }

  private fun refreshDomestic() {
    domesticDisplay.update { prev ->
      cleanupInput(prev.text) { domestic }
    }
  }

  private fun refreshForeign() {
    foreignDisplay.update { prev ->
      cleanupInput(prev.text) { foreign }
    }
  }
}
