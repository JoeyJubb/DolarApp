
package com.joe.dolarApp.presentation.about

import com.joe.dolarApp.presentation.common.DolarAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UiState for the about screen.
 */
data class AboutUiState(
  val message: String,
)

/**
 * ViewModel for the about screen.
 */
@HiltViewModel
class AboutViewModel @Inject constructor() : DolarAppViewModel<AboutUiState>() {

  override val uiState: StateFlow<AboutUiState> = MutableStateFlow(
    AboutUiState("Under construction (about)")
  )

}
