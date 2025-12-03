package com.joe.dolarApp.presentation.calculator

import androidx.compose.ui.text.input.TextFieldValue
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

  enum class ConversionMode {
    /** Selling foreign **/
    ASK,

    /** Buying foreign **/
    BID
  }

  data class ConversionUiState(
    val exchangeRateString: String,
    val mode: ConversionMode,
    val exchangeRate: ExchangeRate,
    val domestic: TextFieldValue,
    val foreign: TextFieldValue,
    val inputError: Boolean,
    val timestamp: String,
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
  object OnRefreshPress : CalculatorUiEvent

  data class OnCurrencySelected(val currencyCode: CurrencyCode) : CalculatorUiEvent
  data class OnBottomSheetVisibilityChanged(val isVisible: Boolean) : CalculatorUiEvent

  data class OnDomesticTextUpdated(val text: TextFieldValue) : CalculatorUiEvent
  data class OnForeignTextUpdated(val text: TextFieldValue) : CalculatorUiEvent
}
