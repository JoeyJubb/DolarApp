package com.joe.dolarApp.util.errorHandling

import com.joe.dolarApp.isFailure
import com.joe.dolarApp.isSuccess
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.fail
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

  @Test
  fun map() {
    // given
    val function: (Int) -> String = { it.toString() }

    // success is mapped
    val success = Result.Success(34)
    val successResult = success.map(function)
    expectThat(successResult).isSuccess().isEqualTo("34")

    // failure isn't mapped
    val failure = Result.Failure(Unit)
    val failureResult = failure.map(function)
    expectThat(failureResult) isSameInstanceAs failure
  }

  @Test
  fun onSuccess() {
    // success invokes the function
    val function : (Unit) -> Unit = mockk(relaxed = true)
    Result.Success(Unit).onSuccess(function)
    verify(exactly = 1) { function.invoke(Unit) }

    // failure does not invoke the function
    Result.Failure(Unit).onSuccess { fail("Result.Failure should not invoke this function") }
  }

  @Test
  fun onFailure() {
    // success does not invoke the function
    Result.Success(Unit).onFailure { fail("Result.Success should not invoke this function") }

    // failure invokes the function
    val function : (Unit) -> Unit = mockk(relaxed = true)
    Result.Failure(Unit).onFailure(function)
    verify(exactly = 1) { function.invoke(Unit) }
  }

  @Test
  fun flatMap() {
    // given
    val function: (Int) -> Result<String, Unit> = { "$it".asSuccess() }

    // success is mapped
    val success = Result.Success(34)
    val successResult = success.flatMap (function)
    expectThat(successResult).isSuccess().isEqualTo("34")

    // failure isn't mapped
    val failure = Result.Failure(Unit)
    val failureResult = failure.flatMap(function)
    expectThat(failureResult) isSameInstanceAs failure
  }

  @Test
  fun recover() {
    // given
    val function: (Int) -> Result<String, Unit> = { "$it I recovered!".asSuccess() }

    // success isn't recovered
    val success = Result.Success(34)
    val successResult = success.recover (function)
    expectThat(successResult).isSuccess().isEqualTo(34) // not same instance due to typing

    // failure is recovered
    val failure = Result.Failure(67)
    val failureResult = failure.recover(function)
    expectThat(failureResult).isSuccess().isEqualTo("67 I recovered!")
  }

  @Test
  fun fold() {
    // given
    val function: (Int) -> String = { "$it" }

    // fold success
    Result.Success(34)
      .fold(
        onSuccess = function,
        onFailure = {fail()}
      )
      .let {
        expectThat(it) isEqualTo "34"
      }

    // fold failure
    Result.Failure(56)
      .fold(
        onSuccess = {fail()},
        onFailure = function,
      )
      .let {
        expectThat(it) isEqualTo "56"
      }
  }

  @Test
  fun getOrNull() {
    // success
    expectThat(
      Result.Success(34).getOrNull()
    ) isEqualTo 34

    // failure
    expectThat(
      Result.Failure(34).getOrNull()
    ) isEqualTo null
  }

}