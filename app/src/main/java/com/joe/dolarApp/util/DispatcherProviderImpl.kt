package com.joe.dolarApp.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

class DispatcherProviderImpl @Inject constructor() : DispatcherProvider {
  override val main: CoroutineDispatcher get() = Dispatchers.Main
  override val io: CoroutineDispatcher get() = Dispatchers.IO
  override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}