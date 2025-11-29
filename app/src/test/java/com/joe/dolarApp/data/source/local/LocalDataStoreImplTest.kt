package com.joe.dolarApp.data.source.local

import CoroutineTestRule
import com.joe.dolarApp.domain.Conversion
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.isFailure
import com.joe.dolarApp.isSuccess
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import io.mockk.slot
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

class LocalDataStoreImplTest {

  @get:Rule
  val testRule: CoroutineTestRule = CoroutineTestRule.asyncOperationsExecuted()

  private val exchangeRateDao: ExchangeRateDao = mockk()

  private val sut: LocalDataStore by lazy {
    LocalDataStoreImpl(exchangeRateDao)
  }

  @Test
  fun `upsert happy path`() = testRule.runTest {
    // given
    val captureSlot = slot<LocalExchangeRate>()
    coJustRun { exchangeRateDao.upsert(capture(captureSlot)) }
    val timeStamp = mockk<LocalDateTime>()

    // when
    val result = sut.upsert(
      ExchangeRate(
        currencyCode = CurrencyCode(CURRENCY_CODE),
        ask = Conversion(123L),
        bid = Conversion(456L),
        timeStamp = timeStamp,
      )
    )

    // then
    expectThat(result).isSuccess()
    expectThat(captureSlot.captured) {
      get { currencyCode } isEqualTo CURRENCY_CODE
      get { askTenDecimals } isEqualTo 123L
      get { bidTenDecimals } isEqualTo 456L
      get { this.timeStamp } isSameInstanceAs timeStamp
    }
  }

  @Test
  fun `get happy path`() = testRule.runTest {
    // given
    val timeStamp = mockk<LocalDateTime>()
    coEvery { exchangeRateDao.getByCurrencyCode(CURRENCY_CODE) } returns LocalExchangeRate(
      currencyCode = CURRENCY_CODE,
      askTenDecimals = 123L,
      bidTenDecimals = 456L,
      timeStamp = timeStamp,
    )

    // when
    val result = sut.get(CurrencyCode(CURRENCY_CODE))

    // then
    expectThat(result).isSuccess()
      .and {
        get { currencyCode }.get { value } isEqualTo CURRENCY_CODE
        get { ask }.get { tenDecimalPlaces } isEqualTo 123L
        get { bid }.get { tenDecimalPlaces } isEqualTo 456L
        get { this.timeStamp } isSameInstanceAs timeStamp
      }
  }

  @Test
  fun `get failure path - item not found`() = testRule.runTest {
    // given
    coEvery { exchangeRateDao.getByCurrencyCode(CURRENCY_CODE) } returns null

    // when
    val result = sut.get(CurrencyCode(CURRENCY_CODE))

    // then
    expectThat(result)
      .isFailure()
  }

  private companion object {
    private const val CURRENCY_CODE = "currency_code"
  }
}