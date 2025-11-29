package com.joe.dolarApp.util.errorHandling

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [Value] or
 * a failure of type [Error]
 *
 * Differs from [kotlin.Result] in that this allows the Failure type to be more descriptive.
 */
sealed interface Result<out Value, out Error> {

  data class Success<Value>(val value: Value) : Result<Value, Nothing>

  data class Failure<Error>(val error: Error) : Result<Nothing, Error>
}

typealias Optional<Value> = Result<Value, Unit>