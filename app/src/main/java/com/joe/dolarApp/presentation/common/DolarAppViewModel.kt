package com.joe.dolarApp.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joe.dolarApp.presentation.calculator.CalculatorUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.publish
import kotlinx.coroutines.launch

abstract class DolarAppViewModel<ViewState, ViewEvent> : ViewModel() {

  abstract val uiState: StateFlow<ViewState>

  private val eventQueue = MutableSharedFlow<ViewEvent>()

  protected abstract suspend fun handleEvent(event: ViewEvent)


  init {
    viewModelScope.launch {
      eventQueue.collect { handleEvent(it) }
    }
  }

  fun onEvent(event: ViewEvent){
    viewModelScope.launch {
      eventQueue.emit(event)
    }
  }


}