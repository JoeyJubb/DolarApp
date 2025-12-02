package com.joe.dolarApp.presentation.calculator

import androidx.lifecycle.viewModelScope
import com.joe.dolarApp.domain.ConversionRepository
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ErrorUiState
import com.joe.dolarApp.presentation.common.DolarAppViewModel
import com.joe.dolarApp.util.LoadState
import com.joe.dolarApp.util.LoadState.Failure
import com.joe.dolarApp.util.LoadState.Loading
import com.joe.dolarApp.util.LoadState.Success
import com.joe.dolarApp.util.WhileUiSubscribed
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asLoadState
import com.joe.dolarApp.util.errorHandling.map
import com.joe.dolarApp.util.errorHandling.mapError
import com.joe.dolarApp.util.errorHandling.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject


/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor(
  private val repo: ConversionRepository,
  private val delegate: ConversionDelegate,
  private val errorStateProvider: ErrorStateProvider,
) : DolarAppViewModel<LoadState<CalculatorUiState, ErrorUiState>, CalculatorUiEvent>() {

  // could be injected
  private val domesticCurrency = CurrencyCode("USDC")

  private val isDataInvalid = MutableStateFlow(true)

  private val showCurrencySelection = MutableStateFlow(false)

  private val foreignCurrencies: Flow<Result<List<CurrencyCode>, ErrorUiState>> =
    isDataInvalid
      .filter { it }
      .map {
        repo.getAvailableForeignCodes(domesticCurrency)
          .mapError {
            errorStateProvider.createErrorState(
              networkError = it
            )
          }
          .onSuccess { onCurrencySelected(it.first()) }
      }

  private val exchangeRateLoadState =
    MutableStateFlow<LoadState<ExchangeRate, ErrorUiState>>(Loading)

  private val conversionState: Flow<Result<CalculatorUiState.ConversionUiState, ErrorUiState>> =
    delegate.observe()
      .map {
        it.mapError {
          errorStateProvider.createGenericError()
        }
      }

  override val uiState: StateFlow<LoadState<CalculatorUiState, ErrorUiState>> = combine(
    conversionState.startWithNull(),
    foreignCurrencies.startWithNull(),
    showCurrencySelection,
    exchangeRateLoadState,
  ) { conversionState, currencyList, showCurrencyList, exchangeRateLoadState ->

    return@combine when (currencyList) {
      is Result.Failure -> Failure(currencyList.error)
      is Result.Success -> when (exchangeRateLoadState) {
        is Failure -> Failure(exchangeRateLoadState.error)
        else -> when (conversionState) {
          is Result.Failure -> Failure(conversionState.error)
          is Result.Success -> {
            Success(
              CalculatorUiState(
                conversion = conversionState.value,
                currencySelection = currencyList.value,
                isCurrencySelectionVisible = showCurrencyList,
                isRefreshing = exchangeRateLoadState is Loading,
              )
            )
          }

          null -> Loading
        }
      }

      null -> Loading
    }
  }.stateIn(
    scope = viewModelScope,
    started = WhileUiSubscribed,
    initialValue = Loading,
  )

  private suspend fun onCurrencySelected(foreign: CurrencyCode) {
    showCurrencySelection.update { false }
    exchangeRateLoadState.update { Loading }
    repo
      .getExchangeRate(domestic = domesticCurrency, foreign = foreign)
      .onSuccess {
        delegate.setExchangeRate(it)
      }
      .mapError { errorStateProvider.createErrorState(it) }
      .let { result ->
        exchangeRateLoadState.update { result.asLoadState() }
      }
    isDataInvalid.update { false }

  }

  override suspend fun handleEvent(event: CalculatorUiEvent) = when (event) {
    CalculatorUiEvent.OnHideCurrencySelectionPress -> showCurrencySelection.update { false }
    CalculatorUiEvent.OnRetryPress -> retry()
    CalculatorUiEvent.OnShowCurrencySelectionPress -> showCurrencySelection.update { true }
    CalculatorUiEvent.OnSwapDirectionPress -> delegate.flip()
    is CalculatorUiEvent.OnCurrencySelected -> onCurrencySelected(event.currencyCode)
    is CalculatorUiEvent.OnBottomSheetVisibilityChanged -> showCurrencySelection.update { event.isVisible }
    is CalculatorUiEvent.OnDomesticTextUpdated -> delegate.onDomesticUpdated(event.text)
    is CalculatorUiEvent.OnForeignTextUpdated -> delegate.onForeignUpdated(event.text)
  }

  private fun retry() = isDataInvalid.update { true }


  private fun <T> Flow<T>.startWithNull(): Flow<T?> =
    this.map { it as T? }.onStart { emit(null) }


}
