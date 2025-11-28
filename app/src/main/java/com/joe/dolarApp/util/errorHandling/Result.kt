package com.joe.dolarApp.util.errorHandling

sealed interface Result<out Value, out Error> {

  data class ExpectedFailure<Error>(val error: Error) : Result<Nothing, Error>

  sealed interface Basic<out Value> : Result<Value, Nothing>{

    data class Success<Value>(val value: Value) : Basic<Value>

    data class UnexpectedFailure(val cause: Throwable) : Basic<Nothing>

  }
}
