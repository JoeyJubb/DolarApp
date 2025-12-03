package com.joe.dolarApp.presentation.calculator

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.joe.dolarApp.R
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ConversionMode
import com.joe.dolarApp.presentation.common.ResourceProvider
import com.joe.dolarApp.presentation.common.TimeStampFormatter
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

  suspend fun onDomesticUpdated(text: TextFieldValue)
  suspend fun onForeignUpdated(text: TextFieldValue)

}

class ConversionDelegateImpl @Inject constructor(
  private val currencyExchanger: CurrencyExchanger,
  private val formatProvider: CurrencyFormatterProvider,
  private val resourceProvider: ResourceProvider,
  private val timestampFormatter: TimeStampFormatter,
) : ConversionDelegate {

  private data class TextHolder(
    val domestic: TextFieldValue = TextFieldValue("0"),
    val foreign: TextFieldValue = TextFieldValue("0")
  )

  /**
   * Changes the text but maintains the selection
   */
  private fun TextFieldValue.withText(text: String) =
    TextFieldValue(
      text = text,
      selection = TextRange(
        start = if (selection.length > 0) text.indexOfFirst { it.isDigit() } else text.length,
        end = text.length
      )
    )

  enum class LastUpdated {
    DOMESTIC, FOREIGN
  }

  private val lastUserUpdated = MutableStateFlow<LastUpdated>(LastUpdated.DOMESTIC)

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
      domestic = text.domestic.withText(
        text = format(text.domestic.text, exchangeRate.domestic)
      ),
      foreign = text.foreign.withText(
        format(text.foreign.text, exchangeRate.foreign)
      ),
      inputError = exchangeError,
      exchangeRateString = resourceProvider.getString(
        R.string.conversion_string,
        format("1", exchangeRate.domestic, false),
        format(getCurrentRate(), exchangeRate.foreign, false),
      ),
      timestamp = timestampFormatter(exchangeRate.timeStamp)
        .getOrDefault(exchangeRate.timeStamp.toString())
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

  override suspend fun flip() {
    mode.update {
      when (it) {
        ConversionMode.ASK -> ConversionMode.BID
        ConversionMode.BID -> ConversionMode.ASK
      }
    }
    refresh()
  }

  override suspend fun setExchangeRate(exchangeRate: ExchangeRate) {
    this.exchangeRate.update {
      exchangeRate
    }
    refresh()
  }

  private suspend fun refresh() {
    // The field the user typed in most recently is kept, the other is updated
    when (lastUserUpdated.value) {
      LastUpdated.DOMESTIC -> onDomesticUpdated(text.value.domestic)
      LastUpdated.FOREIGN -> onForeignUpdated(text.value.foreign)
    }
  }

  override suspend fun onDomesticUpdated(text: TextFieldValue) {
    lastUserUpdated.update { LastUpdated.DOMESTIC }
    exchangeRate.value?.let { exchangeRate ->
      this.text.update {
        val parsed = parseTypedInput(text.text, exchangeRate.domestic)
        TextHolder(
          domestic = text.withText(parsed),
          foreign = it.foreign.withText(
            calculateOther(
              text = parsed,
              invert = false
            )
          )
        )
      }
    }
  }

  override suspend fun onForeignUpdated(text: TextFieldValue) {
    lastUserUpdated.update { LastUpdated.FOREIGN }
    exchangeRate.value?.let { exchangeRate ->
      this.text.update {
        val parsed = parseTypedInput(text.text, exchangeRate.foreign)
        TextHolder(
          foreign = text.withText(parsed),
          domestic = it.domestic.withText(
            calculateOther(
              text = parsed,
              invert = true
            )
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
