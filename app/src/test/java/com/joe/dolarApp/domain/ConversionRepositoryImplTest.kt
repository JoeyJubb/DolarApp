package com.joe.dolarApp.domain

import CoroutineTestRule
import com.joe.dolarApp.TestClock
import com.joe.dolarApp.data.source.local.LocalDataStore
import com.joe.dolarApp.data.source.network.NetworkDataSource
import com.joe.dolarApp.isFailure
import com.joe.dolarApp.isSuccess
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.nanoseconds

class ConversionRepositoryImplTest {

  @get:Rule
  val testRule = CoroutineTestRule.asyncOperationsExecuted()

  private val local: LocalDataStore = mockk()
  private val network: NetworkDataSource = mockk()
  private val clock: Clock = TestClock()

  private val sut: ConversionRepository by lazy {
    ConversionRepositoryImpl(
      clock = clock,
      local = local,
      network = network,
    )
  }

  @Test
  fun getAvailableForeignCodes() = runTest {
    // given
    val networkResult = Result.Failure(mockk<NetworkError>())
    coEvery { network.getCurrencyCodes(domestic) } returns networkResult

    // when
    val result = sut.getAvailableForeignCodes(domestic)

    // then
    expectThat(result) isSameInstanceAs networkResult
  }

  @Test
  fun `getAvailableForeignCodes (empty)`() = runTest {
    // given
    coEvery { network.getCurrencyCodes(domestic) } returns emptyList<CurrencyCode>().asSuccess()

    // when
    val result = sut.getAvailableForeignCodes(domestic)

    // then
    expectThat(result).isFailure()
  }

  @Test
  fun `getExchangeRate (cache hit)`() = runTest {
    // given
    val justInTheNickOfTimeStamp: Instant = clock.now() - (maximumCacheAge - 1.nanoseconds)
    val fromLocal = exchangeRate(timeStamp = justInTheNickOfTimeStamp)
    coEvery { local.get(domestic, foreign) } returns fromLocal.asSuccess()

    // when
    val result = sut.getExchangeRate(domestic, foreign)

    // then
    expectThat(result)
      .isSuccess()
      .isEqualTo(fromLocal)
    coVerify { network wasNot Called }
  }

  @Test
  fun `getExchangeRate (cache miss)`() = runTest {
    // given
    val fromNetwork = exchangeRate()
    val upsertSlot = slot<ExchangeRate>()
    coEvery { local.get(domestic, foreign) } returns Result.Failure(Unit)
    coJustRun { local.upsert(capture(upsertSlot)) }
    coEvery { network.getExchangeRate(domestic, foreign) } returns fromNetwork.asSuccess()

    // when
    val result = sut.getExchangeRate(domestic, foreign)

    // then
    expectThat(result)
      .isSuccess()
      .isEqualTo(fromNetwork)

    expectThat(upsertSlot.captured)
      .isEqualTo(fromNetwork)
  }

  @Test
  fun `getExchangeRate (cache too old)`() = runTest {
    // given
    val tooOldTimeStamp: Instant = clock.now() - maximumCacheAge
    val fromLocal = exchangeRate(timeStamp = tooOldTimeStamp)
    val fromNetwork = exchangeRate()
    val upsertSlot = slot<ExchangeRate>()
    coEvery { local.get(domestic, foreign) } returns fromLocal.asSuccess()
    coJustRun { local.upsert(capture(upsertSlot)) }
    coEvery { network.getExchangeRate(domestic, foreign) } returns fromNetwork.asSuccess()

    // when
    val result = sut.getExchangeRate(domestic, foreign)

    // then
    expectThat(result)
      .isSuccess()
      .isEqualTo(fromNetwork)

    expectThat(upsertSlot.captured)
      .isEqualTo(fromNetwork)
  }

  @Test
  fun `getExchangeRate (cache too old, network failure)`() = runTest {
    // given
    val tooOldTimeStamp: Instant = clock.now() - maximumCacheAge
    val fromLocal = exchangeRate(timeStamp = tooOldTimeStamp)
    coEvery { local.get(domestic, foreign) } returns fromLocal.asSuccess()
    coEvery {
      network.getExchangeRate(
        domestic,
        foreign
      )
    } returns Result.Failure(NetworkError.Disconnected)

    // when
    val result = sut.getExchangeRate(domestic, foreign)

    // then
    expectThat(result)
      .isSuccess()
      .isEqualTo(fromLocal)
  }

  @Test
  fun `getExchangeRate (force refresh)`() = runTest {
    // given
    val fromNetwork = exchangeRate()
    val upsertSlot = slot<ExchangeRate>()
    coJustRun { local.upsert(capture(upsertSlot)) }
    coEvery { network.getExchangeRate(domestic, foreign) } returns fromNetwork.asSuccess()

    // when
    val result = sut.getExchangeRate(domestic, foreign, forceRefresh = true)

    // then
    expectThat(result)
      .isSuccess()
      .isEqualTo(fromNetwork)

    expectThat(upsertSlot.captured)
      .isEqualTo(fromNetwork)

    coVerify(exactly = 0) { local.get(any(), any()) }
  }

  private fun exchangeRate(
    domestic: CurrencyCode = Companion.domestic,
    foreign: CurrencyCode = Companion.foreign,
    timeStamp: Instant = clock.now(),
  ): ExchangeRate = mockk<ExchangeRate> {
    every { this@mockk.domestic } returns domestic
    every { this@mockk.foreign } returns foreign
    every { this@mockk.timeStamp } returns timeStamp
  }

  private companion object {
    private val maximumCacheAge = 2.hours
    private val domestic = CurrencyCode("domestic")
    private val foreign = CurrencyCode("foreign")
  }

}