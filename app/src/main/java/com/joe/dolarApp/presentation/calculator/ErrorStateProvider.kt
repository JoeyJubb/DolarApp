package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.presentation.common.ResourceProvider
import com.joe.dolarApp.util.errorHandling.NetworkError
import javax.inject.Inject

class ErrorStateProvider @Inject constructor(
  private val resourceProvider: ResourceProvider,
) {

  fun createErrorState(networkError: NetworkError) : CalculatorUiState.ErrorUiState {
    return when(networkError){
      is NetworkError.ClientFailure -> CalculatorUiState.ErrorUiState(
        message = "TDOD -> ${networkError.cause.message}",
        canRetry = false
      )
      NetworkError.Disconnected -> CalculatorUiState.ErrorUiState(
        message = "TODO message Not connected",
        canRetry = true
      )
      is NetworkError.NetworkFailure -> CalculatorUiState.ErrorUiState(
        message = "TODO message network failure",
        canRetry = true
      )
    }
  }

  fun createGenericError() = CalculatorUiState.ErrorUiState(
    message = "TODO generic error",
    canRetry = true
  )

}
