package com.joe.dolarApp.presentation.calculator

import android.content.Context
import android.os.Build
import androidx.collection.LruCache
import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.onSuccess
import com.joe.dolarApp.util.errorHandling.tryCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject

interface CurrencyFormatter {

  fun toCurrencyString(
    string: String,
    roundToDecimals: Boolean = true,
  ): Result<String, Throwable>

  fun parseTypedInput(string: String): Result<String, Throwable>
}

interface CurrencyFormatterProvider {

  operator fun get(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable>
}

class CurrencyFormatterProviderImpl @Inject constructor(
  @param:ApplicationContext private val applicationContext: Context,
) : CurrencyFormatterProvider {

  private val locale by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      applicationContext.resources.configuration.locales[0]
    } else {
      applicationContext.resources.configuration.locale
    }
  }

  private val lruCache = LruCache<CurrencyCode, CurrencyFormatter>(maxSize = 2)

  override operator fun get(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable> {
    return lruCache[currencyCode]?.asSuccess()
      ?: load(currencyCode).onSuccess { lruCache.put(currencyCode, it) }

  }

  private fun load(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable> = tryCatching {

    val baseCurrencyCode = currencyCode.value.take(3)

    val currency = Currency.getInstance(baseCurrencyCode)!!
    val decimalPlaces = currency.defaultFractionDigits

    val prefix = when (currencyCode.value) {
      "USDC" -> "USDC$"
      else -> currency.getSymbol(locale)
    }

    CurrencyFormatterImpl(
      numberFormat = NumberFormat.getInstance(locale)
        .apply {
          this.currency = currency
          minimumFractionDigits = decimalPlaces
          maximumFractionDigits = decimalPlaces
        },
      prefix = prefix,
      decimalPlaces = decimalPlaces,
    )
  }
}

class CurrencyFormatterImpl(
  private val numberFormat: NumberFormat,
  private val decimalPlaces: Int,
  private val prefix: String,
) : CurrencyFormatter {


  override fun toCurrencyString(string: String, roundToDecimals: Boolean): Result<String, Throwable> = tryCatching {

    numberFormat.maximumFractionDigits = if(roundToDecimals) decimalPlaces else 10
    numberFormat
      .format(string.toBigDecimal())
      .let { "$prefix$it" }
  }

  override fun parseTypedInput(string: String): Result<String, Throwable> = tryCatching {
    string
      .removePrefix(prefix)
      .filter { it.isDigit() }
      .take(CurrencyExchanger.PRECISION)
      .ifBlank { "0" }
      .toBigDecimal()
      .setScale(decimalPlaces)
      .divide(BigDecimal.TEN.pow(decimalPlaces))
      .toPlainString()
  }
}
