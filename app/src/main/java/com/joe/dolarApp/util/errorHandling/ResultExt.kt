package com.joe.dolarApp.util.errorHandling

fun <T> T.asSuccess(): Result.Success<T> = Result.Success(this)

fun <E> E.asFailure() : Result.Failure<E> = Result.Failure(this)

fun <T> T?.asResult() : Optional<T> = this?.asSuccess() ?: Result.Failure(Unit)