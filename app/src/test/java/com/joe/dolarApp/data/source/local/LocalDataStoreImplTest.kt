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
        domestic = DOMESTIC,
        foreign = FOREIGN,
        ask = "ask",
        bid = "bid",
        timeStamp = timeStamp,
      )
    )

    // then
    expectThat(result).isSuccess()
    expectThat(captureSlot.captured) {
      get { domesticCurrency } isEqualTo DOMESTIC.value
      get { foreignCurrency } isEqualTo FOREIGN.value
      get { ask } isEqualTo "ask"
      get { bid } isEqualTo "bid"
      get { this.timeStamp } isSameInstanceAs timeStamp
    }
  }

  @Test
  fun `get happy path`() = testRule.runTest {
    // given
    val timeStamp = mockk<Instant>()
    coEvery { exchangeRateDao.getExchangeRate(any(), any()) } answers {
      LocalExchangeRate(
        domesticCurrency = firstArg(),
        foreignCurrency = secondArg(),
        ask = "ask",
        bid = "bid",
        timeStamp = timeStamp,
      )
    }

    // when
    val result = sut.get(DOMESTIC, FOREIGN)

    // then
    expectThat(result).isSuccess()
      .and {
        get { domestic } isEqualTo DOMESTIC
        get { foreign } isEqualTo FOREIGN
        get { ask } isEqualTo "ask"
        get { bid } isEqualTo "bid"
        get { this.timeStamp } isSameInstanceAs timeStamp
      }
  }

  @Test
  fun `get failure path - item not found`() = testRule.runTest {
    // given
    coEvery { exchangeRateDao.getExchangeRate(DOMESTIC.value, FOREIGN.value) } returns null

    // when
    val result = sut.get(DOMESTIC, FOREIGN)

    // then
    expectThat(result)
      .isFailure()
  }

  private companion object {
    private val DOMESTIC = CurrencyCode("dom")
    private val FOREIGN = CurrencyCode("for")
  }
}