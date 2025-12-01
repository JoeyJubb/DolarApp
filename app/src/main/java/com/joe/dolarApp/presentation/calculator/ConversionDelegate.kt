package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.CurrencyInputUiState
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.onFailure
import com.joe.dolarApp.util.errorHandling.onSuccess
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
  )

  fun observe(): Flow<Result<State, Unit>>

  /**
   * Swaps from and to currencies
   */
  suspend fun flip()

  /**
   * Sets the non-USDc currency
   */
  suspend fun setExchangeRate(exchangeRate: ExchangeRate)

}

class ConversionDelegateImpl @Inject constructor(
  private val currencyExchanger: CurrencyExchanger,
) : ConversionDelegate {

  private data class TextDisplay(
    val text: String = "",
    val error: Boolean = false,
  )

  private val exchangeRate = MutableStateFlow<ExchangeRate?>(null)
  private val isDomesticToForeign = MutableStateFlow(true)

  private val domesticDisplay = MutableStateFlow(TextDisplay())

  private val foreignDisplay = MutableStateFlow<TextDisplay>(TextDisplay())

  override suspend fun flip() {
    isDomesticToForeign.updateAndGet { !it }
    refresh()
  }

  override suspend fun setExchangeRate(exchangeRate: ExchangeRate) {
    this.exchangeRate.update { exchangeRate }
    refresh()
  }

  override fun observe(): Flow<Result<ConversionDelegate.State, Unit>> =
    combine(
      isDomesticToForeign,
      exchangeRate.filterNotNull(),
      domesticDisplay,
      foreignDisplay,
    ) { isDomesticToForeign, exchangeRate, domesticDisplay, foreignDisplay ->

      val rate = if(isDomesticToForeign) exchangeRate.bid else exchangeRate.ask

      val domestic = CurrencyInputUiState(
        currency = exchangeRate.domestic,
        display = domesticDisplay.text,
        error = domesticDisplay.error,
        showCountryPicker = false,
        onTextChanged = {
          onDomesticChanged(it)
          refreshForeign(
            value = it,
            rate = rate,
            domestic = exchangeRate.domestic,
            foreign = exchangeRate.foreign
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
          refreshDomestic(
            value = it,
            rate = rate,
            domestic = exchangeRate.domestic,
            foreign = exchangeRate.foreign
          )
        },
      )

      val (from, to) =  if(isDomesticToForeign) domestic to foreign else foreign to domestic

      ConversionDelegate.State(
        from = from,
        to = to
      ).asSuccess()
    }

  private suspend fun refreshForeign(
    value: String,
    rate: String,
    domestic: CurrencyCode,
    foreign: CurrencyCode
  ) {
    currencyExchanger.doExchange(
      value = value,
      rate = rate,
      from = domestic,
      to = foreign,
      invertRate = false
    ).onSuccess { result ->
      foreignDisplay.update { TextDisplay(result) }
    }.onFailure {
      Timber.e(it)
      domesticDisplay.update { prev -> prev.copy(error = true) }
    }
  }

  private suspend fun refreshDomestic(
    value: String,
    rate: String,
    domestic: CurrencyCode,
    foreign: CurrencyCode,
  ) {
    currencyExchanger.doExchange(
      value = value,
      rate = rate,
      from = foreign,
      to = domestic,
      invertRate = true
    ).onSuccess { result ->
      domesticDisplay.update { TextDisplay(result) }
    }.onFailure {
      Timber.e(it)
      foreignDisplay.update { prev -> prev.copy(error = true) }
    }
  }

  private fun onDomesticChanged(string: String) {
    domesticDisplay.update { TextDisplay(string) }
  }

  private fun onForeignChanged(string: String) {
    foreignDisplay.update { TextDisplay(string) }
  }

  private suspend fun refresh() {
    exchangeRate.value?.let { exchangeRate ->

      val isDomesticToForeign = isDomesticToForeign.value
      val rate = if(isDomesticToForeign) exchangeRate.bid else exchangeRate.ask

      if(isDomesticToForeign){
        refreshForeign(
          value = domesticDisplay.value.text,
          rate = rate,
          domestic = exchangeRate.domestic,
          foreign = exchangeRate.foreign
        )
      }else{
        refreshDomestic(
          value = foreignDisplay.value.text,
          rate = rate,
          domestic = exchangeRate.domestic,
          foreign = exchangeRate.foreign
        )
      }
    }
  }
}
