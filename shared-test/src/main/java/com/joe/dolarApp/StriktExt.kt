package com.joe.dolarApp

import strikt.api.Assertion
import com.joe.dolarApp.util.errorHandling.Result

fun <R, E> Assertion.Builder<out Result<R, E>>.isSuccess(): Assertion.Builder<R> =
  assert("is success") { subject ->
    when(subject){
      is Result.Basic.Success -> {
        pass()
      }
      is Result.Basic.UnexpectedFailure -> {
        println("before fail")
        fail(
          description = "Unexpected failure",
          cause = subject.cause
        )
        println("after fail")
      }
      is Result.ExpectedFailure -> {
        fail(
          description = "Expected failure",
          actual = subject.error,
        )
      }
    }
  }
    .get("value") {
      when(this){
        is Result.Basic.Success -> value
        is Result.ExpectedFailure -> throw IllegalStateException()
        is Result.Basic.UnexpectedFailure -> throw cause
      }
    }

fun <R, E> Assertion.Builder<out Result<R, E>>.isUnexpectedFailure(): Assertion.Builder<Throwable> =
  assert("is failure") { subject ->
    when(subject){
      is Result.Basic.Success -> fail(
        description = "Success",
        actual = subject.value
      )
      is Result.Basic.UnexpectedFailure -> pass()
      is Result.ExpectedFailure -> fail(
        description = "Expected failure failure",
        actual = subject.error,
      )
    }
  }
    .get("value") {
      when(this){
        is Result.Basic.UnexpectedFailure -> cause
        else -> throw IllegalStateException("Shouldn't happen")
      }
    }

fun <R, E> Assertion.Builder<out Result<R, E>>.isExpectedFailure(): Assertion.Builder<E> =
  assert("is failure") { subject ->
    println("subject -> $subject")
    when(subject){
      is Result.Basic.Success -> fail(
        description = "Success",
        actual = subject.value
      )
      is Result.Basic.UnexpectedFailure -> fail(
        description = "Unexpected failure",
        actual = null,
        cause = subject.cause
      )
      is Result.ExpectedFailure -> pass()
    }
  }
    .get("value") {
      when(this){
        is Result.ExpectedFailure -> error
        is Result.Basic.Success -> throw IllegalStateException()
        is Result.Basic.UnexpectedFailure -> throw cause
      }
    }
