package com.joe.dolarApp.data.source.local

import CoroutineTestRule
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.ExchangeRate
import com.joe.dolarApp.isFailure
import com.joe.dolarApp.isSuccess
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import io.mockk.slot
import kotlinx.datetime.Instant
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
    val timeStamp = mockk<Instant>()

    // when
    val result = sut.upsert(
      ExchangeRate(
        currencyCode = CurrencyCode(CURRENCY_CODE),
        ask = "ask",
        bid = "bid",
        timeStamp = timeStamp,
      )
    )

    // then
    expectThat(result).isSuccess()
    expectThat(captureSlot.captured) {
      get { currencyCode } isEqualTo CURRENCY_CODE
      get { ask } isEqualTo "ask"
      get { bid } isEqualTo "bid"
      get { this.timeStamp } isSameInstanceAs timeStamp
    }
  }

  @Test
  fun `get happy path`() = testRule.runTest {
    // given
    val timeStamp = mockk<Instant>()
    coEvery { exchangeRateDao.getByCurrencyCode(CURRENCY_CODE) } returns LocalExchangeRate(
      currencyCode = CURRENCY_CODE,
      ask = "ask",
      bid = "bid",
      timeStamp = timeStamp,
    )

    // when
    val result = sut.get(CurrencyCode(CURRENCY_CODE))

    // then
    expectThat(result).isSuccess()
      .and {
        get { currencyCode }.get { value } isEqualTo CURRENCY_CODE
        get { ask } isEqualTo "ask"
        get { bid } isEqualTo "bid"
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