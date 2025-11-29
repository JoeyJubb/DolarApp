package com.joe.dolarApp.data.source.local

import androidx.room.TypeConverter
import kotlinx.datetime.Instant


class Converters {

  @TypeConverter
  fun toInstant(string: String?): Instant? = string?.let(Instant::parse)

  @TypeConverter
  fun toString(date: Instant?): String? = date?.toString()
}