package com.joe.dolarApp.util.errorHandling

import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> coRunCatching(
  noinline block: suspend () -> T
): Result.Basic<T> {
  return try {
    block().asSuccess()
  } catch (e: Throwable) {
    if (e is CancellationException) {
      throw e
    }
    Result.Basic.UnexpectedFailure(e)
  }
}

fun <T> T.asSuccess(): Result.Basic.Success<T> = Result.Basic.Success(this)

inline infix fun <T, U, E> Result.Basic<T>.flatMap(transform: (T) -> Result<U, E>) : Result<U, E>{
  return when (this){
    is Result.Basic.Success -> transform(value)
    is Result.Basic.UnexpectedFailure -> this
  }
}
