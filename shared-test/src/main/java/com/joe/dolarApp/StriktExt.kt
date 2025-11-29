package com.joe.dolarApp

import strikt.api.Assertion
import com.joe.dolarApp.util.errorHandling.Result

fun <R, E> Assertion.Builder<out Result<R, E>>.isSuccess(): Assertion.Builder<R> =
  assert("is success") { subject ->
    when(subject){
      is Result.Success -> {
        pass()
      }
      is Result.Failure -> {
        fail(
          description = "Expected failure",
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
    println("subject -> $subject")
    when(subject){
      is Result.Success -> fail(
        description = "Success",
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
