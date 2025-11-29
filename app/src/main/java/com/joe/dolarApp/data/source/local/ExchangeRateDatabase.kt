
package com.joe.dolarApp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The Room Database that contains the Task table.
 *
 * Note that exportSchema should be true in production databases.
 */
@TypeConverters(Converters::class)
@Database(entities = [LocalExchangeRate::class], version = 1, exportSchema = false)
abstract class ExchangeRateDatabase : RoomDatabase() {

  abstract fun exchangeRateDao(): ExchangeRateDao
}
