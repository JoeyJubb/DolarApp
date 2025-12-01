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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joe.dolarApp.R
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ConversionUiState
import com.joe.dolarApp.presentation.common.CalculatorTopBar
import com.joe.dolarApp.util.LoadState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  openDrawer: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalculatorViewModel = hiltViewModel(),
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val onEvent = viewModel::onEvent

  val childModifier = modifier.fillMaxSize()

  when (val state = uiState) {
    is LoadState.Failure -> {
      NoBottomSheet(
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
        content = { paddingValues -> ErrorContent(
          error = state.error,
          onEvent = onEvent,
          modifier = childModifier.padding(paddingValues)
        ) })
    }

    LoadState.Loading -> {
      NoBottomSheet(
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
        content = { paddingValues -> LoadingContent(childModifier.padding(paddingValues)) }
      )
    }

    is LoadState.Success -> {
      CalculatorContent(
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
        uiState = state.value,
        onEvent = onEvent,
        modifier = childModifier
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorContent(
  openDrawer: () -> Unit,
  snackbarHostState: SnackbarHostState,
  uiState: CalculatorUiState,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  val scaffoldState = rememberBottomSheetScaffoldState(
    bottomSheetState = rememberModalBottomSheetState(
      skipPartiallyExpanded = true
    )
  )

  LaunchedEffect(uiState.isCurrencySelectionVisible) {
    if (uiState.isCurrencySelectionVisible) {
      scaffoldState.bottomSheetState.show()
    } else {
      scaffoldState.bottomSheetState.hide()
    }
  }

  LaunchedEffect(scaffoldState.bottomSheetState.isVisible) {
    onEvent(CalculatorUiEvent.OnBottomSheetVisibilityChanged(scaffoldState.bottomSheetState.isVisible))
  }

  BottomSheetScaffold(
    modifier = modifier.fillMaxSize(),
    scaffoldState = scaffoldState,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = { CalculatorTopBar(openDrawer) },
    sheetContent = {
      CurrencyList(
        state = uiState.currencySelection,
        onEvent = onEvent,
      )
    }
  ) { paddingValues ->

    Conversion(
      state = uiState.conversion,
      onEvent = onEvent,
      modifier = modifier.padding(paddingValues)
    )
  }
}

@Composable
fun Conversion(
  state: ConversionUiState,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = spacedBy(8.dp)
  ) {

    Text(state.conversionRateString)

    TextArea(state.from, onEvent)

    Button(
      onClick = { onEvent(CalculatorUiEvent.OnSwapDirectionPress) },
      content = { Text("Swap") }
    )

    TextArea(state.to, onEvent)

  }
}

@Composable
fun TextArea(
  currencyInputUiState: CalculatorUiState.CurrencyInputUiState,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  TextField(
    modifier = modifier,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    value = currencyInputUiState.display,
    onValueChange = { value ->
      coroutineScope.launch {
        currencyInputUiState.onTextChanged(value)
      }
    },
    isError = currencyInputUiState.error,
    leadingIcon = if (currencyInputUiState.showCountryPicker) {
      {
        Button(
          onClick = { onEvent(CalculatorUiEvent.OnShowCurrencySelectionPress) },
          content = { Text("Pick") }
        )
      }
    } else {
      null
    }
  )
}

@Composable
private fun CurrencyList(
  state: List<CurrencyCode>,
  onEvent: (CalculatorUiEvent) -> Unit
) {
  LazyColumn {
    items(state) { currencyCode ->
      ListItem(
        modifier = Modifier.clickable {
          onEvent(CalculatorUiEvent.OnCurrencySelected(currencyCode))
        },
        headlineContent = { Text(currencyCode.value) },

        )
    }
  }
}


@Composable
fun NoBottomSheet(
  openDrawer: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = { CalculatorTopBar(openDrawer) },
    content = content
  )
}

@Composable
private fun LoadingContent(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator()
  }
}

@Composable
private fun ErrorContent(
  error: CalculatorUiState.ErrorUiState,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
  ) {
    Text(error.message)

    if (error.canRetry) {
      Button(
        onClick = { onEvent(CalculatorUiEvent.OnRetryPress) },
        content = { Text(stringResource(R.string.btn_action_retry)) },
      )
    }
  }
}

@Preview
@Composable
fun CalculatorScreenPreview() {
  Surface {
    CalculatorScreen({})
  }
}