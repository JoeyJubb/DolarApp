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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.joe.dolarApp.R
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ConversionUiState
import com.joe.dolarApp.presentation.common.CalculatorTopBar
import com.joe.dolarApp.util.LoadState

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

  val childModifier = modifier
    .imePadding()
    .navigationBarsPadding()
    .padding(horizontal = 16.dp, vertical = 24.dp)
    .fillMaxSize()

  when (val state = uiState) {
    is LoadState.Failure -> {
      NoBottomSheet(
        modifier = childModifier,
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
        content = { paddingValues ->
          ErrorContent(
            error = state.error,
            onEvent = onEvent,
            modifier = Modifier.padding(paddingValues)
          )
        })
    }

    LoadState.Loading -> {
      NoBottomSheet(
        modifier = childModifier,
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
        content = { paddingValues -> LoadingContent(Modifier.padding(paddingValues)) }
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
      skipPartiallyExpanded = true,
    )
  )
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(uiState.isCurrencySelectionVisible) {
    if (uiState.isCurrencySelectionVisible) {
      keyboardController?.hide()
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
        modifier = Modifier.navigationBarsPadding(),
        state = uiState.currencySelection,
        onEvent = onEvent,
      )
    }
  ) { paddingValues ->

    Conversion(
      state = uiState.conversion,
      isRefreshing = uiState.isRefreshing,
      onEvent = onEvent,
      modifier = modifier.padding(paddingValues)
    )
  }
}

@Composable
fun Conversion(
  state: ConversionUiState,
  isRefreshing: Boolean,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()),
    verticalArrangement = spacedBy(8.dp),
  ) {

    val textAreaModifier = Modifier.fillMaxWidth()

    Text(
      text = state.exchangeRateString
    )
    Text(
      text = stringResource(R.string.rates_accurate_at, state.exchangeRate.timeStamp),
    )

    @Composable
    fun domestic() {
      TextArea(
        value = state.domestic,
        onValueChange = { onEvent(CalculatorUiEvent.OnDomesticTextUpdated(it)) },
        modifier = textAreaModifier,
        currencyCode = state.exchangeRate.domestic,
        onEvent = onEvent,
        isError = state.inputError,
      )
    }

    @Composable
    fun foreign() {
      TextArea(
        value = state.foreign,
        onValueChange = { onEvent(CalculatorUiEvent.OnForeignTextUpdated(it)) },
        modifier = textAreaModifier,
        currencyCode = state.exchangeRate.foreign,
        onEvent = onEvent,
        showCountryPicker = true,
        isError = state.inputError,
      )
    }

    @Composable
    fun swapButton() {
      Button(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        onClick = { onEvent(CalculatorUiEvent.OnSwapDirectionPress) },
        content = {
          Icon(
            painter = painterResource(R.drawable.baseline_arrow_downward_24),
            contentDescription = stringResource(R.string.btn_currency_swap)
          )
        },
      )
    }
    when (state.mode) {
      CalculatorUiState.ConversionMode.ASK -> {
        foreign()
        swapButton()
        domestic()
      }

      CalculatorUiState.ConversionMode.BID -> {
        domestic()
        swapButton()
        foreign()
      }
    }

    AnimatedVisibility(isRefreshing) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
  }
}

@Composable
fun TextArea(
  value: String,
  onValueChange: (String) -> Unit,
  currencyCode: CurrencyCode,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
  showCountryPicker: Boolean = false,
  isError: Boolean,
) {
  //TODO figure out how to allow for Select All

  TextField(
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    leadingIcon = {
      TextAreaLeading(
        currencyCode = currencyCode,
        showCountryPicker = showCountryPicker,
        onEvent = onEvent,
      )
    },
    modifier = modifier,
    textStyle = LocalTextStyle.current.copy(
      textAlign = TextAlign.End,
    ),
    isError = isError,
    onValueChange = {
      onValueChange(it.text)
    },
    value = TextFieldValue(
      text = value,
      selection = TextRange(value.length)
    )
  )
}

@Composable
private fun TextAreaLeading(
  currencyCode: CurrencyCode,
  showCountryPicker: Boolean,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    horizontalArrangement = spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .clickable(
        enabled = showCountryPicker,
        onClickLabel = stringResource(R.string.btn_choose_currency)
      ) {
        onEvent(CalculatorUiEvent.OnShowCurrencySelectionPress)
      }
  ) {
    Spacer(Modifier.size(4.dp))

    CountryFlag(
      currency = currencyCode,
      modifier = modifier,
    )
    Text(currencyCode.value)

    AnimatedVisibility(showCountryPicker) {
      Icon(
        painter = painterResource(R.drawable.outline_arrow_drop_down_24),
        contentDescription = null // decorative
      )
    }
  }
}

@Composable
private fun CountryFlag(
  currency: CurrencyCode,
  size: Dp = 16.dp,
  modifier: Modifier = Modifier,
) {
  AsyncImage(
    modifier = modifier.size(size),
    model = "https://flagsapi.com/${currency.value.take(2)}/flat/64.png",
    contentDescription = currency.value,
  )
}

@Composable
private fun CurrencyList(
  state: List<CurrencyCode>,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(modifier = modifier) {
    LazyColumn {
      items(state) { currencyCode ->
        ListItem(
          modifier = Modifier.clickable {
            onEvent(CalculatorUiEvent.OnCurrencySelected(currencyCode))
          },
          headlineContent = { Text(currencyCode.value) },
          leadingContent = { CountryFlag(currencyCode) }
        )
      }
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