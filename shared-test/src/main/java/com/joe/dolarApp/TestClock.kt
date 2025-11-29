package com.joe.dolarApp

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class TestClock(var now: Instant = Instant.fromEpochMilliseconds(0)) : Clock{

  override fun now(): Instant = now

}