package com.joe.dolarApp.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val unconfined: CoroutineDispatcher
}