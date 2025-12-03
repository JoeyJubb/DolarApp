package com.joe.dolarApp.domain

import com.joe.dolarApp.util.DispatcherProvider
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.coTryCatching
import com.joe.dolarApp.util.errorHandling.onFailure
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject

interface CurrencyExchanger {

  /**
   * Accepts a formatted [value], but does not return a formatted result
   */
  suspend fun doExchange(
    value: String,
    rate: String,
    invertRate: Boolean,
  ): Result<String, Throwable>

  companion object{
    /**
     * The maximum number of currency digits to display to the user.
     */
    const val PRECISION = 500
  }
}

class CurrencyExchangerImpl @Inject constructor(
  private val dispatcherProvider: DispatcherProvider,
) : CurrencyExchanger {

  override suspend fun doExchange(
    value: String,
    rate: String,
    invertRate: Boolean,
  ): Result<String, Throwable> = coTry {
    value
      .parse()
      .let {
        val multiplicand = rate.toBigDecimal()
        if (invertRate) it.divide(multiplicand, MathContext.DECIMAL128)
        else it.multiply(multiplicand)
      }
      .toPlainString()
  }

  private suspend fun <T> coTry(function: suspend () -> T): Result<T, Throwable> =
    withContext(dispatcherProvider.unconfined) {
      coTryCatching {
        function()
      }
        .onFailure { Timber.e(it) }
    }

  private fun String.parse(): BigDecimal = when {
    isBlank() -> BigDecimal.ZERO
    else -> this.toBigDecimal()
  }

}
