package com.joe.dolarApp.data.source.local

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime


class Converters {

  @TypeConverter
  fun toDate(string: String?): LocalDateTime? = string?.let(LocalDateTime::parse)

  @TypeConverter
  fun toString(date: LocalDateTime?): String? = date?.toString()
}