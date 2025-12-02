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

package com.joe.dolarApp.util

/**
 * A generic class that holds a loading signal or the result of an async operation.
 */
sealed interface LoadState<out T, out V> {

  object Loading : LoadState<Nothing, Nothing>

  data class Success<out Value>(val value: Value) : LoadState<Value, Nothing>

  data class Failure<out Error>(val error: Error) : LoadState<Nothing, Error>
}

