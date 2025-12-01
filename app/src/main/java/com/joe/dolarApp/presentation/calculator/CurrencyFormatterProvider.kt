package com.joe.dolarApp.presentation.calculator

import android.content.Context
import androidx.core.text.isDigitsOnly
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.map
import com.joe.dolarApp.util.errorHandling.mapError
import com.joe.dolarApp.util.errorHandling.tryCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.RuntimeException
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject

interface CurrencyFormatter {
  fun format(bigDecimal: BigDecimal): Result<String, Throwable>
  fun parse(string: String): Result<BigDecimal, Throwable>

}

interface CurrencyFormatterProvider {

  operator fun get(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable>
}

class CurrencyFormatterProviderImpl @Inject constructor(
  @param:ApplicationContext private val applicationContext: Context,
) : CurrencyFormatterProvider {

  private val locale by lazy {
    applicationContext.resources.configuration.locales[0]
  }

  override operator fun get(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable> {
    return when (currencyCode.value) {
      "USDC" -> USDCFormatter().asSuccess()
      else -> numberFormatter(currencyCode)
    }
  }

  private fun numberFormatter(currencyCode: CurrencyCode) = tryCatching {
    NumberFormat.getInstance(locale)
      .apply {
        currency = Currency.getInstance(currencyCode.value)!!
      }
  }
    .map(::CurrencyFormatterImpl)
}

class USDCFormatter : CurrencyFormatter {

  override fun format(bigDecimal: BigDecimal): Result<String, Throwable> = bigDecimal
    .toPlainString()
    .asSuccess()

  override fun parse(string: String): Result<BigDecimal, Throwable> = tryCatching {
    string.toBigDecimal()
  }
}

class CurrencyFormatterImpl(
  val numberFormat: NumberFormat,
) : CurrencyFormatter {

  override fun format(bigDecimal: BigDecimal): Result<String, Throwable> = tryCatching {

    numberFormat.format(
    bigDecimal
  )
  }.mapError {
    RuntimeException("format(${numberFormat.currency}, $bigDecimal), ${it.message}", it )
  }

  override fun parse(string: String): Result<BigDecimal, Throwable> = tryCatching {
    numberFormat.parse(string)!!.toString().toBigDecimal()
  }


}
