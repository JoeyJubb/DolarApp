package com.joe.dolarApp.util.errorHandling

import kotlin.time.Duration

sealed interface NetworkError{

  data object Disconnected : NetworkError

  /**
   * Network responded with a success, but this app could not understand the response.
   *
   * Not usually able to recover from this
   */
  data class UnexpectedResponse(
    val cause: Throwable
  ): NetworkError

  /**
   * The network responded with a failure.
   */
  data class BadResponse(
    val retryStrategy: RetryStrategy,
    val debugMessage: String,
  ) : NetworkError{

    sealed interface RetryStrategy{
      data object None : RetryStrategy
      data object Immediate: RetryStrategy
      data class AfterDelay(val delay: Duration) : RetryStrategy
    }

  }
}