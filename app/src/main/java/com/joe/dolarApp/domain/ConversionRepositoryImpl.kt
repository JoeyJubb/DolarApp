package com.joe.dolarApp.domain

import com.joe.dolarApp.data.source.local.LocalDataStore
import com.joe.dolarApp.data.source.network.NetworkDataSource
import com.joe.dolarApp.util.errorHandling.NetworkError
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.Result.Failure
import com.joe.dolarApp.util.errorHandling.asFailure
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.flatMap
import com.joe.dolarApp.util.errorHandling.getOrNull
import com.joe.dolarApp.util.errorHandling.onSuccess
import dagger.Reusable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Reusable
class ConversionRepositoryImpl @Inject constructor(
  private val clock: Clock,
  private val local: LocalDataStore,
  private val network: NetworkDataSource,
) : ConversionRepository {

  override suspend fun getExchangeRate(
    domestic: CurrencyCode,
    foreign: CurrencyCode,
    forceRefresh: Boolean
  ): Result<ExchangeRate, NetworkError> = if (forceRefresh) {
    fromNetwork(domestic, foreign)
  } else {
    /* Check the cache first */
    val local = local.get(domestic, foreign).getOrNull()

    if (local == null || local.timeStamp.isTooOld()) {
      /* Cache miss or the cached value is too old */
      when (val network = fromNetwork(domestic, foreign)) {
        /* network failed! attempt to use the cached value even if it's too old */
        is Failure -> local?.asSuccess() ?: network
        /* network success -- use the up to date value */
        is Result.Success -> network
      }
    } else {
      local.asSuccess()
    }
  }


  private suspend fun fromNetwork(
    domestic: CurrencyCode,
    foreign: CurrencyCode
  ): Result<ExchangeRate, NetworkError> =
    network.getExchangeRate(domestic, foreign)
      .onSuccess { exchangeRate ->
        local.upsert(exchangeRate)
      }

  private fun Instant.isTooOld(): Boolean = clock.now() - this >= maximumCacheAge

  /*
   * Is this feature envy? It looks like it might be, but it's okay for now. We don't want
   * ViewModels depending on data sources directly. ViewModels should depend on repositories or
   * usecases.
   *
   * Perhaps we'll want to add some local caching here later? Much easier to do if all the
   * ViewModels that need this info are pointed at this repository!
   */
  override suspend fun getAvailableForeignCodes(domestic: CurrencyCode): Result<List<CurrencyCode>, NetworkError> =
    network.getCurrencyCodes(domestic)
      .flatMap {
        if (it.isEmpty()) {
          NetworkError.NetworkFailure(debugMessage = "List is empty").asFailure()
        } else {
          it.asSuccess()
        }
      }


  private companion object {

    /**
     * If the local entry is at least this old, automatically try the network.
     */
    private val maximumCacheAge = 2.hours
  }
}
