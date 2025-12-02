package com.joe.dolarApp.domain.fakes

import com.joe.dolarApp.domain.CurrencyCode
import com.joe.dolarApp.presentation.calculator.CurrencyFormatter
import com.joe.dolarApp.presentation.calculator.CurrencyFormatterProvider
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.asSuccess
import com.joe.dolarApp.util.errorHandling.tryCatching
import java.math.BigDecimal

class FakeCurrencyFormatterProvider : CurrencyFormatterProvider {

  override fun get(currencyCode: CurrencyCode): Result<CurrencyFormatter, Throwable> =
    FakeCurrencyFormatter(currencyCode).asSuccess()


  private class FakeCurrencyFormatter(val currencyCode: CurrencyCode) : CurrencyFormatter {

    override fun format(bigDecimal: BigDecimal): Result<String, Throwable> {
      return "${currencyCode.value} ${bigDecimal.toPlainString()}".asSuccess()
    }

    /** doesn't do any parsing */
    override fun parse(string: String): Result<BigDecimal, Throwable> = tryCatching {
      string.toBigDecimal()
    }

  }

}
