package com.joe.dolarApp.util.errorHandling

fun <T> T.asSuccess(): Result.Success<T> = Result.Success(this)

fun <E> E.asFailure(): Result.Failure<E> = Result.Failure(this)

fun <T> T?.asResult(): Optional<T> = this?.asSuccess() ?: Result.Failure(Unit)

fun <T, E1, E2> Result<T, E1>.mapError(transform : (E1)-> E2): Result<T, E2> = when(this){
  is Result.Failure -> Result.Failure(transform(error))
  is Result.Success -> this
}