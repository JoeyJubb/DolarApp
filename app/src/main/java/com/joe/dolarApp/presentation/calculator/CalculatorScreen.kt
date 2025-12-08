package com.joe.dolarApp.presentation.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    .fillMaxSize()


  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = { CalculatorTopBar(openDrawer) },
  ) { paddingValues ->

    when (val state = uiState) {
      is LoadState.Failure -> {
        ErrorContent(
          error = state.error,
          onEvent = onEvent,
          modifier = Modifier.padding(paddingValues)
        )
      }

      LoadState.Loading -> {
        LoadingContent(Modifier.padding(paddingValues))
      }

      is LoadState.Success -> {
        CalculatorContent(
          uiState = state.value,
          onEvent = onEvent,
          modifier = childModifier.padding(paddingValues)
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorContent(
  uiState: CalculatorUiState,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(uiState.isCurrencySelectionVisible) {
    if (uiState.isCurrencySelectionVisible) {
      keyboardController?.hide()
    }
  }

  Box(
    modifier = modifier,
  ){

    Conversion(
      state = uiState.conversion,
      isRefreshing = uiState.isRefreshing,
      onEvent = onEvent,
    )

    if(uiState.isCurrencySelectionVisible) {
      ModalBottomSheet(
        onDismissRequest = { onEvent(CalculatorUiEvent.OnHideCurrencySelectionPress) },
        content = {
          CurrencyList(
            state = uiState.currencySelection,
            onEvent = onEvent,
          )
        }
      )
    }
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
    modifier = modifier
      .verticalScroll(rememberScrollState()),
    verticalArrangement = spacedBy(16.dp),
  ) {

    ListItem(
      modifier = Modifier.clickable {
        onEvent(CalculatorUiEvent.OnRefreshPress)
      },
      headlineContent = { Text(text = state.exchangeRateString) },
      supportingContent = { Text(stringResource(R.string.rates_accurate_at, state.timestamp)) },
      trailingContent = {
        Icon(
          painter = painterResource(R.drawable.outline_refresh_24),
          contentDescription = stringResource(R.string.btn_action_refresh)
        )
      }
    )

    val textAreaModifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxWidth()

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
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  currencyCode: CurrencyCode,
  onEvent: (CalculatorUiEvent) -> Unit,
  modifier: Modifier = Modifier,
  showCountryPicker: Boolean = false,
  isError: Boolean,
) {
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
      onValueChange(it)
    },
    value = value
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
  LazyColumn(
    modifier = modifier,
  ) {
    items(state) { currencyCode ->
      ListItem(
        modifier = Modifier.clickable {
          onEvent(CalculatorUiEvent.OnCurrencySelected(currencyCode))
        },
        headlineContent = { Text(currencyCode.value) },
        leadingContent = { CountryFlag(currencyCode) }
      )
    }
    item {  }
  }
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
        onClick = { onEvent(CalculatorUiEvent.OnRefreshPress) },
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