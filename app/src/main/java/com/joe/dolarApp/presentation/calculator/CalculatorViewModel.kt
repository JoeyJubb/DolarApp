/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
