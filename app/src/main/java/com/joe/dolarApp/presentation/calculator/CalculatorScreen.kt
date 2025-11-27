/*
 * Copyright 2022 The Android Open Source Project
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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joe.dolarApp.R
import com.joe.dolarApp.presentation.common.CalculatorTopBar

@Composable
fun CalculatorScreen(
  openDrawer: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalculatorViewModel = hiltViewModel(),
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = { CalculatorTopBar(openDrawer) },
  ) { paddingValues ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalculatorContent(
      uiState = uiState,
      modifier = modifier.padding(paddingValues)
    )
  }
}

@Composable
private fun CalculatorContent(
  uiState: CalculatorUiState,
  modifier: Modifier = Modifier
) {
  Text(
    text = uiState.message,
    modifier = modifier
      .fillMaxSize()
      .padding(all = dimensionResource(id = R.dimen.horizontal_margin))
  )
}

@Preview
@Composable
fun CalculatorScreenPreview() {
  Surface {
    CalculatorScreen({})
  }
}