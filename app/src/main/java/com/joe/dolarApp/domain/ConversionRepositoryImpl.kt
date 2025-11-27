/*
 * Copyright 2019 The Android Open Source Project
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

package com.joe.dolarApp.domain

import dagger.Reusable
import javax.inject.Inject
import kotlin.Result.Companion.failure

@Reusable
class ConversionRepositoryImpl @Inject constructor(
) : ConversionRepository {

  override suspend fun getExchangeRate(
    currency: CurrencyCode,
    forceRefresh: Boolean
  ): Result<ExchangeRate> = failure(NotImplementedError())


  override suspend fun getAvailableCurrencies(forceRefresh: Boolean): Result<List<CurrencyCode>> = failure(NotImplementedError())
}
