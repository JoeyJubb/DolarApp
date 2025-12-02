package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate

/**
 * UiState for the calculator screen.
 */
data class CalculatorUiState(
  val conversion: ConversionUiState,
  val currencySelection: List<CurrencyCode>,
  val isCurrencySelectionVisible: Boolean,
  val isRefreshing: Boolean,
) {

  enum class ConversionMode{
    /** Selling foreign **/
    ASK,
    /** Buying foreign **/
    BID
  }

  data class ConversionUiState(
    val mode: ConversionMode,
    val exchangeRate: ExchangeRate,
    val domestic: TextState,
    val foreign: TextState,
    val inputError: Boolean,
  )

  data class TextState(
    val value: String,
    val format: (String) -> String,
  )

  data class ErrorUiState(
    val message: String,
    val canRetry: Boolean,
  )
}

sealed interface CalculatorUiEvent {

  object OnShowCurrencySelectionPress : CalculatorUiEvent
  object OnHideCurrencySelectionPress : CalculatorUiEvent
  object OnSwapDirectionPress : CalculatorUiEvent
  object OnRetryPress : CalculatorUiEvent

  data class OnCurrencySelected(val currencyCode: CurrencyCode) : CalculatorUiEvent
  data class OnBottomSheetVisibilityChanged(val isVisible: Boolean) : CalculatorUiEvent

  data class OnDomesticTextUpdated(val text: String) : CalculatorUiEvent
  data class OnForeignTextUpdated(val text: String) : CalculatorUiEvent
}
