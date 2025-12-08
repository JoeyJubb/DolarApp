package com.joe.dolarApp.domain

import CoroutineTestRule
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.joe.dolarApp.isFailure
import com.joe.dolarApp.isSuccess
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(TestParameterInjector::class)
class CurrencyExchangerImplTest {

  @get:Rule
  val testRule = CoroutineTestRule.asyncOperationsExecuted()


  private val sut: CurrencyExchanger by lazy {
    CurrencyExchangerImpl(
      dispatcherProvider = testRule.testDispatcherProvider,
    )
  }

  @Test
  fun `happy path`(
    @TestParameter testCase: HappyPathTestCase,
  ) = testRule.runTest {
    // when
    val result = sut.doExchange(
      value = testCase.givenInput,
      rate = testCase.givenExchangeRate,
      invertRate = testCase.givenInvertedRate,
    )

    // then
    expectThat(result).isSuccess() isEqualTo testCase.expect
  }

  @Test
  fun `failure path`(
    @TestParameter testCase: FailurePathTestCase,
  ) = testRule.runTest {
    // when
    val result = sut.doExchange(
      value = testCase.givenInput,
      rate = "1",
      invertRate = false,
    )

    // then
    expectThat(result).isFailure()
  }

  enum class HappyPathTestCase(
    val givenInput: String,
    val givenExchangeRate: String,
    val givenInvertedRate: Boolean = false,
    val expect: String,
  ) {
    BLANK_INPUT(
      givenInput = "",
      givenExchangeRate = "8.4",
      expect = "0.0"
    ),
    ZERO_INPUT(
      givenInput = "0",
      givenExchangeRate = "8.4",
      expect = "0.0"
    ),
    NEGATIVE_INPUT(
      givenInput = "-2",
      givenExchangeRate = "8.4",
      expect = "-16.8"
    ),
    ZERO_EXCHANGE(
      givenInput = "2.5",
      givenExchangeRate = "0",
      expect = "0.0"
    ),
    CAN_CHECK_THIS_ON_PAPER(
      givenInput = "2.5",
      givenExchangeRate = "8.4",
      expect = "21.00"
    ),
    VERY_LARGE_PRECISION(
      givenInput = "1.0000000000000000000000000024",
      givenExchangeRate = "84000000000000000000000000.023",
      expect = "84000000000000000000000000.2246000000000000000000000000552"
    ),
    INVERTED(
      givenInput = "100.00",
      givenExchangeRate = "5",
      expect = "20.00",
      givenInvertedRate = true,
    ),
    INVERTED_REQUIRES_ROUNDING(
      givenInput = "100.00",
      givenExchangeRate = "3",
      expect = "33.33333333333333333333333333333333333333333333333333333333333333",
      givenInvertedRate = true,
    ),
    FRACTIONS(
      givenInput = "0.01",
      givenExchangeRate = "18.29383",
      expect = "0.1829383"
    )
  }


  enum class FailurePathTestCase(
    val givenInput: String,
  ) {
    TOO_MANY_DECIMALS(
      givenInput = "2.5.0",
    ),

    NOT_A_NUMBER(
      givenInput = "The quick brown fox jumps over the lazy dog"
    )
  }
}