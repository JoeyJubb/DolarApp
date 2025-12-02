package com.joe.dolarApp.presentation.common

import android.content.Context
import android.text.format.DateFormat
import com.joe.dolarApp.util.errorHandling.Result
import com.joe.dolarApp.util.errorHandling.tryCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class TimeStampFormatter @Inject constructor(
  @ApplicationContext private val appContext: Context,
) {

  private val locale by lazy {
    appContext.resources.configuration.locales[0]
  }

  @OptIn(FormatStringsInDatetimeFormats::class)
  operator fun invoke(instant: Instant): Result<String, Throwable> = tryCatching {
    LocalDateTime.Format {
      byUnicodePattern(
        DateFormat.getBestDateTimePattern(
          locale,
          "yyyy-MM-dd HH:mm:ss"
        )
      )
    }.format(
      instant.toLocalDateTime(TimeZone.currentSystemDefault())
    )
  }

}
