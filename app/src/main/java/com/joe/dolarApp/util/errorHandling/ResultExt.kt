package com.joe.dolarApp.util.errorHandling

import com.joe.dolarApp.util.LoadState
import kotlinx.coroutines.CancellationException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun <T> T.asSuccess(): Result.Success<T> = Result.Success(this)

fun <E> E.asFailure(): Result.Failure<E> = Result.Failure(this)

fun <T> T?.asResult(): Optional<T> = this?.asSuccess() ?: Result.Failure(Unit)


inline fun <T, E1, E2> Result<T, E1>.mapError(transform: (E1) -> E2): Result<T, E2> = when (this) {
  is Result.Failure -> Result.Failure(transform(error))
  is Result.Success -> this
}

inline fun <T, E> Result<T, E>.onSuccess(function: (T) -> Unit): Result<T, E> {
  if (this is Result.Success) {
    function(value)
  }
  return this
}

inline fun <T, E> Result<T, E>.onFailure(function: (E) -> Unit): Result<T, E> {
  if (this is Result.Failure) {
    function(error)
  }
  return this
}

inline fun <T1, T2, E> Result<T1, E>.map(
  function: (T1) -> T2
): Result<T2, E> = when (this) {
  is Result.Failure -> this
  is Result.Success -> function(value).asSuccess()
}

inline fun <T1, T2, E> Result<T1, E>.flatMap(
  function: (T1) -> Result<T2, E>
): Result<T2, E> = when (this) {
  is Result.Failure -> this
  is Result.Success -> function(value)
}

inline fun <T, E1, E2> Result<T, E1>.recover(
  function: (E1) -> Result<T, E2>
): Result<T, E2> = when (this) {
  is Result.Failure -> function(error)
  is Result.Success -> Result.Success(value)
}

fun <T1, T2, E> Result<T1, E>.fold(
  onSuccess: (T1) -> T2,
  onFailure: (E) -> T2,
): T2 = when (this) {
  is Result.Failure -> onFailure(error)
  is Result.Success -> onSuccess(value)
}

fun <T, E> Result<T, E>.getOrNull(): T? = (this as? Result.Success)?.value
fun <T> Result<T, Throwable>.getOrThrow(): T = when (this) {
  is Result.Failure -> throw this.error
  is Result.Success -> this.value
}

fun <T, E> Result<T, E>.getOrDefault(default: T): T = getOrNull() ?: default

fun <T> tryCatching(function: () -> T): Result<T, Throwable> =
  try {
    function().asSuccess()
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    e.asFailure()
  }

suspend fun <T> coTryCatching(function: suspend () -> T): Result<T, Throwable> =
  try {
    function().asSuccess()
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    e.asFailure()
  }

@OptIn(ExperimentalContracts::class)
fun <V, E> Result<V, E>.isFailure(): Boolean {
  contract {
    returns(true) implies (this@isFailure is Result.Failure)
    returns(false) implies (this@isFailure is Result.Success)
  }
  return this is Result.Failure
}


fun <T, E> Result<T, E>.asLoadState(): LoadState<T, E> = when (this) {
  is Result.Failure -> LoadState.Failure(error)
  is Result.Success -> LoadState.Success(value)
}