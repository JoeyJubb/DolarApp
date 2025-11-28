package com.joe.dolarApp

import com.joe.dolarApp.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatcherProvider(
  override val main: CoroutineDispatcher,
  override val io: CoroutineDispatcher,
  override val unconfined: CoroutineDispatcher,
) : DispatcherProvider {

  constructor(dispatcher: TestDispatcher) : this(
    main = dispatcher,
    io = dispatcher,
    unconfined = dispatcher,
  )

  val dispatcher: TestDispatcher
    get() = main as TestDispatcher
}
