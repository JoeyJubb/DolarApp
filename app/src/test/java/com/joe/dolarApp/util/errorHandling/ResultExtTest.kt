package com.joe.dolarApp.util.errorHandling

import com.joe.dolarApp.isFailure
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

class ResultExtTest {


  @Test
  fun mapError() {
    // given
    val function: (Int) -> String = { it.toString() }

    // success isn't mapped
    val success = Result.Success(Unit)
    val successResult = success.mapError(function)
    expectThat(successResult) isSameInstanceAs success

    // failure is mapped
    val failure = Result.Failure(23)
    val failureResult = failure.mapError(function)
    expectThat(failureResult).isFailure().isEqualTo("23")
  }

}