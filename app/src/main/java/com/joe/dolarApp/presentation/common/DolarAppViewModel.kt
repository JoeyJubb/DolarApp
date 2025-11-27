package com.joe.dolarApp.presentation.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class DolarAppViewModel<ViewState> : ViewModel() {

  abstract val uiState: StateFlow<ViewState>

}