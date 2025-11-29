
package com.joe.dolarApp.presentation.calculator

import com.joe.dolarApp.presentation.common.DolarAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UiState for the calculator screen.
 */
data class CalculatorUiState(
  val message: String,
)

/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor() : DolarAppViewModel<CalculatorUiState>() {

  override val uiState: StateFlow<CalculatorUiState> get() = MutableStateFlow(CalculatorUiState("Under construction (calculator)"))

}
