package com.joe.dolarApp.presentation.calculator

import CoroutineTestRule
import app.cash.turbine.test
import com.joe.dolarApp.domain.ConversionRepository
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.isLoaded
import com.joe.dolarApp.presentation.calculator.CalculatorUiState.ConversionUiState
import com.joe.dolarApp.util.errorHandling.asSuccess
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse

class CalculatorViewModelTest {

  @get:Rule
  val testRule = CoroutineTestRule.asyncOperationsExecuted()

  private val repo: ConversionRepository = mockk()
  private val delegate: ConversionDelegate = mockk()
  private val errorStateProvider: ErrorStateProvider = mockk()

  private val sut: CalculatorViewModel by lazy {
    CalculatorViewModel(
      repo = repo,
      delegate = delegate,
      errorStateProvider = errorStateProvider,
    )
  }

  @Test
  fun `happy path loaded state`() = testRule.runTest {
    // given
    val fromRepo = listOf(CurrencyCode(FOREIGN))
    coEvery {
      repo.getAvailableForeignCodes(
        domestic = any()
      )
    } returns fromRepo.asSuccess()

    coEvery {
      repo.getExchangeRate(
        domestic = any(),
        foreign = any()
      )
    } answers {
      exchangeRateMockk(
        domesticCode = firstArg(),
        foreignCode = secondArg()
      ).asSuccess()
    }

    val fromDelegate = mockk<ConversionUiState>()
    val exchangeRateSlot = slot<ExchangeRate>()
    every { delegate.observe() } returns flowOf(fromDelegate)
    coJustRun { delegate.setExchangeRate(capture(exchangeRateSlot)) }

    // when
    sut.uiState.test {
      expectThat(awaitItem()).isLoaded().and {
        get { conversion } isEqualTo fromDelegate
        get { currencySelection } isEqualTo fromRepo
        get { isCurrencySelectionVisible }.isFalse()
        get { isRefreshing }.isFalse()
      }

      expectNoEvents()
    }

    // then
    expectThat(exchangeRateSlot.captured) {
      get { domestic } isEqualTo CurrencyCode(DOMESTIC)
      get { foreign } isEqualTo CurrencyCode(FOREIGN)
    }

  }


  private fun exchangeRateMockk(
    domesticCode: String = DOMESTIC,
    foreignCode: String = FOREIGN,
  ) = mockk<ExchangeRate> {
    every { foreign } returns CurrencyCode(foreignCode)
    every { domestic } returns CurrencyCode(domesticCode)
  }

  private companion object {
    private const val DOMESTIC = "USDC"
    private const val FOREIGN = "FOREIGN"
  }
}
