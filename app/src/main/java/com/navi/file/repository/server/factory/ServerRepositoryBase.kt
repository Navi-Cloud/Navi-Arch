package com.navi.file.repository.server.factory

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.navi.file.model.ErrorResponseModel
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import retrofit2.Call
import retrofit2.Response

abstract class ServerRepositoryBase {
    // ObjectMapper
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Get Server-Communication Result, in custom Execution Result
     * It will automatically handle with given 'handled case' but, if not, it will return ExecutionResult with unknown type.
     * @param T Response Type
     * @param handledCase Handled Case that we are expecting
     * @return Execution Result.
     */
    protected fun<T> Call<T>.getExecutionResult(handledCase: HashMap<Int, (Response<T>) -> ExecutionResult<T>>): ExecutionResult<T> {
        return runCatching {
            val response = this.execute()
            handleCommunication(handledCase, response)
        }.getOrElse {
            ExecutionResult(ResultType.Connection, null, "Unknown Error Occurred!")
        }
    }

    /**
     * Get Error Message From Response.
     * May return server-side sent error message, or it will create unknown-default error message.
     *
     * @param T Response Type
     * @param response Response itself
     * @return Error Message.
     */
    protected fun<T> getErrorMessage(response: Response<T>): String {
        return runCatching {
            objectMapper.readValue<ErrorResponseModel>(response.errorBody().toString()).message
        }.getOrElse {
            "Unknown Error Occurred!"
        }
    }

    /**
     * Handle Communication Result in abstract way.
     * It will automatically return 'unknown error' if handledCase does not contains corresponding
     * respond code.
     *
     * @param T Response Type
     * @param handledCase An expected case that we can handle in lazy perspective.
     * @param response Actual Response
     * @return ExecutionResult
     */
    private fun <T> handleCommunication(handledCase: HashMap<Int, (Response<T>) -> ExecutionResult<T>>, response: Response<T>): ExecutionResult<T> {
        return if (handledCase.containsKey(response.code())) {
            handledCase[response.code()]!!.invoke(response)
        } else {
            ExecutionResult(ResultType.Unknown, null, "Unknown Error Occurred!")
        }
    }
}