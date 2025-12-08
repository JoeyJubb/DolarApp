package com.joe.dolarApp

import com.joe.dolarApp.util.LoadState
import com.joe.dolarApp.util.errorHandling.Result
import strikt.api.Assertion

fun <R, E> Assertion.Builder<out Result<R, E>>.isSuccess(): Assertion.Builder<R> =
  assert("is success") { subject ->
    when(subject){
      is Result.Success -> {
        pass()
      }
      is Result.Failure -> {
        fail(
          description = "is a failure",
          actual = subject.error,
        )
      }
    }
  }
    .get("value") {
      when(this){
        is Result.Success -> value
        is Result.Failure -> throw IllegalStateException()
      }
    }

fun <R, E> Assertion.Builder<out Result<R, E>>.isFailure(): Assertion.Builder<E> =
  assert("is failure") { subject ->
    when(subject){
      is Result.Success -> fail(
        description = "is a Success",
        actual = subject.value
      )
      is Result.Failure -> pass()
    }
  }
    .get("value") {
      when(this){
        is Result.Failure -> error
        is Result.Success -> throw IllegalStateException()
      }
    }

fun <R, E> Assertion.Builder<out LoadState<R, E>>.isLoaded(): Assertion.Builder<R> =
  assert("is success") { subject ->
    when (subject) {
      is LoadState.Failure -> {
        fail(
          description = "is a failure",
          actual = subject.error,
        )
      }

      LoadState.Loading -> {
        fail(
          description = "is loading",
          actual = subject,
        )
      }

      is LoadState.Success -> pass()
    }
  }
    .get("value") {
      when (this) {
        is LoadState.Success -> value
        else -> throw IllegalStateException()
      }
    }

fun <R, E> Assertion.Builder<out LoadState<R, E>>.isFailed(): Assertion.Builder<E> =
  assert("is is a failure") { subject ->
    when (subject) {
      is LoadState.Failure -> pass()

      LoadState.Loading -> {
        fail(
          description = "is loading",
          actual = subject,
        )
      }

      is LoadState.Success -> {
        fail(
          description = "is a success",
          actual = subject.value,
        )
      }
    }
  }
    .get("value") {
      when (this) {
        is LoadState.Failure -> error
        else -> throw IllegalStateException()
      }
    }

fun <R, E> Assertion.Builder<out LoadState<R, E>>.isLoading(): Assertion.Builder<LoadState.Loading> =
  assert("is loading") { subject ->
    when (subject) {
      is LoadState.Failure -> {
        fail(
          description = "is a failure",
          actual = subject.error,
        )
      }

      LoadState.Loading -> pass()

      is LoadState.Success -> {
        fail(
          description = "is a success",
          actual = subject.value,
        )
      }
    }
  }
    .get("value") {
      when (this) {
        is LoadState.Loading -> this
        else -> throw IllegalStateException()
      }
    }
