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
import java.util.concurrent.atomic.AtomicBoolean
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

  private val isFirstLoad = AtomicBoolean(true)
  private val isDataInvalid = MutableStateFlow(true)

  private val showCurrencySelection = MutableStateFlow(false)

  private val foreignCurrencies: Flow<Result<List<CurrencyCode>, ErrorUiState>> =
    isDataInvalid
      .filter { it }
      .map {
        repo.getAvailableForeignCodes(domesticCurrency)
          .mapError(errorStateProvider::createErrorState)
          .onSuccess { onCurrencySelected(it.first()) }
          .also { isDataInvalid.update { false } }
      }

  private val exchangeRate =
    MutableStateFlow<LoadState<ExchangeRate, ErrorUiState>>(Loading)

  override val uiState: StateFlow<LoadState<CalculatorUiState, ErrorUiState>> = combine(
    isDataInvalid,
    delegate.observe().startWithNull(),
    foreignCurrencies.startWithNull(),
    showCurrencySelection,
    exchangeRate,
  ) { isDataInvalid, conversionState, currencyList, showCurrencyList, exchangeRateLoadState ->

    return@combine when (currencyList) {
      is Result.Failure -> Failure(currencyList.error)
      is Result.Success -> when (exchangeRateLoadState) {
        is Failure -> Failure(exchangeRateLoadState.error)
        else -> when (conversionState) {
          null -> Loading
          else -> Success(
            CalculatorUiState(
              conversion = conversionState,
              currencySelection = currencyList.value,
              isCurrencySelectionVisible = showCurrencyList,
              isRefreshing = isDataInvalid || exchangeRateLoadState is Loading,
            )
          )
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
    exchangeRate.update { Loading }
    exchangeRate.update {
      repo
        .getExchangeRate(
          domestic = domesticCurrency,
          foreign = foreign,
          forceRefresh = !isFirstLoad.getAndSet(false)
        )
        .onSuccess { delegate.setExchangeRate(it) }
        .mapError(errorStateProvider::createErrorState)
        .asLoadState()
    }
  }

  override suspend fun handleEvent(event: CalculatorUiEvent) = when (event) {
    CalculatorUiEvent.OnHideCurrencySelectionPress -> showCurrencySelection.update { false }
    CalculatorUiEvent.OnRefreshPress -> retry()
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
