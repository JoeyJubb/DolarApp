package com.joe.dolarApp.util.errorHandling

import kotlin.time.Duration

sealed interface NetworkError {

  data object Disconnected : NetworkError

  /**
   * Network responded with a success, but this app could not parse the response correctly
   *
   * Not usually able to recover from this
   */
  data class ClientFailure(
    val cause: Throwable
  ) : NetworkError

  /**
   * The network responded with a failure.
   */
  data class NetworkFailure(
    val code: Int? = null,
    val retryStrategy: RetryStrategy = RetryStrategy.Immediate,
    val cause: Throwable? = null,
    val debugMessage: String? = cause?.message
  ) : NetworkError {

    sealed interface RetryStrategy {
      data object None : RetryStrategy
      data object Immediate : RetryStrategy
      data class AfterDelay(val delay: Duration) : RetryStrategy
    }
  }
}