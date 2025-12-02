package com.joe.dolarApp.presentation.calculator

import android.content.Context
import android.icu.text.DecimalFormatSymbols
import android.icu.util.Currency
import androidx.collection.LruCache
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.flatMap
import com.joe.dolarApp.util.errorHandling.map
import com.joe.dolarApp.util.errorHandling.onSuccess
import com.joe.dolarApp.util.errorHandling.tryCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency.getInstance
import javax.inject.Inject

interface CurrencyFormatter {
  fun format(bigDecimal: BigDecimal): Result<String, Throwable>
  fun parse(string: String): Result<BigDecimal, Throwable>

  fun format(string: String): Result<String, Throwable> = parse(string).flatMap { format(it) }

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

  private val lruCache = LruCache<CurrencyCode, CurrencyFormatter>(maxSize = 2)


  override operator fun get(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable> {
    return lruCache[currencyCode]?.asSuccess()
      ?: load(currencyCode).onSuccess { lruCache.put(currencyCode, it) }

  }

  private fun load(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable> {

    val formatterCode = currencyCode.value.take(3)
    val prefix = when (currencyCode.value) {
      "USDC" -> "USDC$"
      else -> Currency.getInstance(formatterCode).getSymbol(locale)
    }
    val decimalSeparator = DecimalFormatSymbols(locale).decimalSeparator

    return tryCatching {
      NumberFormat.getInstance(locale)
        .apply {
          this.currency = getInstance(formatterCode)!!
        }
    }
      .map {
        CurrencyFormatterImpl(
          numberFormat = it,
          prefix = prefix,
          decimalSeparator = decimalSeparator
        )
      }
  }

}

class CurrencyFormatterImpl(
  private val numberFormat: NumberFormat,
  private val prefix: String,
  private val decimalSeparator: Char,
) : CurrencyFormatter {

  override fun format(bigDecimal: BigDecimal): Result<String, Throwable> = tryCatching {
    bigDecimal
      .setScale(numberFormat.maximumFractionDigits, RoundingMode.HALF_DOWN)
      .let(numberFormat::format)
      .let {
        "$prefix$it"
      }
  }

  override fun parse(string: String): Result<BigDecimal, Throwable> = tryCatching {
    string.filter {
      it.isDigit() || it == decimalSeparator
    }
      .let(numberFormat::parse)!!
      .toString()
      .toBigDecimal()
  }


}
