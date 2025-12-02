package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.domain.CurrencyCode

/**
 * UiState for the calculator screen.
 */
data class CalculatorUiState(
  val conversion: ConversionUiState,
  val currencySelection: List<CurrencyCode>,
  val isCurrencySelectionVisible: Boolean,
  val isRefreshing: Boolean,
) {

  data class ConversionUiState(
    val conversionRateString: String,
    val timestamp: String,
    val from: CurrencyInputUiState,
    val to: CurrencyInputUiState,
  )

  data class CurrencyInputUiState(
    val currency: CurrencyCode,
    val display: String,
    val error: Boolean,
    val showCountryPicker: Boolean,
    val onTextChanged: suspend (String) -> Unit,
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
}
