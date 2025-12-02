package com.joe.dolarApp.domain

import com.joe.dolarApp.presentation.calculator.CurrencyFormatterProvider
import com.joe.dolarApp.util.DispatcherProvider
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.coTryCatching
import com.joe.dolarApp.util.errorHandling.flatMap
import com.joe.dolarApp.util.errorHandling.getOrThrow
import kotlinx.coroutines.withContext
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
    from: CurrencyCode,
    invertRate: Boolean,
  ): Result<String, Throwable>
}

class CurrencyExchangerImpl @Inject constructor(
  private val dispatcherProvider: DispatcherProvider,
  private val currencyFormatterProvider: CurrencyFormatterProvider,
) : CurrencyExchanger {

  override suspend fun doExchange(
    value: String,
    rate: String,
    from: CurrencyCode,
    invertRate: Boolean,
  ): Result<String, Throwable> = coTry {

    value
      .parse(from)
      .let {
        val multiplicand = rate.toBigDecimal()
        if (invertRate) it.divide(multiplicand, MathContext.DECIMAL64)
        else it.multiply(multiplicand)
      }
      .toPlainString()
  }

  private suspend fun <T> coTry(function: suspend () -> T): Result<T, Throwable> =
    withContext(dispatcherProvider.unconfined) {
      coTryCatching {
        function()
      }
    }

  private fun String.parse(from: CurrencyCode): BigDecimal = when {
    isBlank() -> BigDecimal.ZERO
    else -> currencyFormatterProvider[from]
      .flatMap { it.parse(this) }
      .getOrThrow()
  }

}
