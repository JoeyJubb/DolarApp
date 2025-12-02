/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.joe.dolarApp.di

import android.content.Context
import android.os.SystemClock
import androidx.room.Room
import com.joe.dolarApp.data.source.local.ExchangeRateDao
import com.joe.dolarApp.data.source.local.ExchangeRateDatabase
import com.joe.dolarApp.data.source.local.LocalDataStore
import com.joe.dolarApp.data.source.local.LocalDataStoreImpl
import com.joe.dolarApp.data.source.network.DelegatingNetworkDataSource
import com.joe.dolarApp.data.source.network.NetworkDataSource
import com.joe.dolarApp.data.source.network.FakeNetworkDataSource
import com.joe.dolarApp.data.source.network.NetworkDataSourceImpl
import com.joe.dolarApp.domain.ConversionRepository
import com.joe.dolarApp.domain.ConversionRepositoryImpl
import com.joe.dolarApp.domain.CurrencyExchanger
import com.joe.dolarApp.domain.CurrencyExchangerImpl
import com.joe.dolarApp.presentation.calculator.ConversionDelegate
import com.joe.dolarApp.presentation.calculator.ConversionDelegateImpl
import com.joe.dolarApp.presentation.calculator.CurrencyFormatterProvider
import com.joe.dolarApp.presentation.calculator.CurrencyFormatterProviderImpl
import com.joe.dolarApp.presentation.common.ResourceProvider
import com.joe.dolarApp.presentation.common.ResourceProviderImpl
import com.joe.dolarApp.util.DispatcherProvider
import com.joe.dolarApp.util.DispatcherProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import java.text.DecimalFormatSymbols
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvidesAppModule {

  @Singleton
  @Provides
  fun provideClock(): Clock = Clock.System
}

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

  @Reusable
  @Binds
  fun bindResourceProvider(impl: ResourceProviderImpl): ResourceProvider

  @Reusable
  @Binds
  fun bindCurrencyFormatterProvider(impl: CurrencyFormatterProviderImpl): CurrencyFormatterProvider

  @Reusable
  @Binds
  fun bindCurrencyExchanger(impl: CurrencyExchangerImpl): CurrencyExchanger
}

@Module
@InstallIn(SingletonComponent::class)
interface DataSourceModule {

  @Singleton
  @Binds
  fun bindNetworkDataSource(impl: DelegatingNetworkDataSource): NetworkDataSource

  @Singleton
  @Binds
  fun bindLocalDataStore(impl: LocalDataStoreImpl): LocalDataStore
}
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Singleton
  @Provides
  fun provideDataBase(@ApplicationContext context: Context): ExchangeRateDatabase {
    return Room.databaseBuilder(
      context.applicationContext,
      ExchangeRateDatabase::class.java,
      "CurrencyConversions.db"
    )
      .build()
  }

  @Provides
  fun provideTaskDao(database: ExchangeRateDatabase): ExchangeRateDao = database.exchangeRateDao()
}


@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {

  @Reusable
  @Binds
  fun bindConversionRepository(impl: ConversionRepositoryImpl): ConversionRepository

  @Reusable
  @Binds
  fun bindConversionDelegate(impl: ConversionDelegateImpl): ConversionDelegate
}
