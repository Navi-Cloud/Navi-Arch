package com.navi.file.repository.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.navi.file.model.ErrorResponseModel
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

enum class ResultType {
    Success,
    Conflict,
    Forbidden,
    Connection,
    Unknown
}

class ExecutionResult<T>(
    var resultType: ResultType,
    var value: T?,
    var message: String
)

class ServerRepository(
    httpUrl: HttpUrl
) {

    // ObjectMapper
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    // Create ApiInterface
    private val serverRestApi: ApiInterface = Retrofit.Builder()
        .baseUrl(httpUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create()

    /**
     * Register user to server.
     *
     * @param userRegisterRequest User Register Request, containing some id, password, etc.
     * @return ExecutionResult,
     * Containing Result Type. But this does not return any data object, but only for checking server communication succeed.
     */
    fun registerUser(userRegisterRequest: UserRegisterRequest): ExecutionResult<ResponseBody> {
        // Handled Case
        val handledCase: HashMap<Int, (Response<ResponseBody>) -> ExecutionResult<ResponseBody>> = hashMapOf(
            HttpURLConnection.HTTP_OK to {
                ExecutionResult(ResultType.Success, value = null, message = "")
            },
            HttpURLConnection.HTTP_CONFLICT to {
                ExecutionResult(ResultType.Conflict, value = null, message = getErrorMessage(it))
            }
        )

        return serverRestApi.registerUser(userRegisterRequest).getExecutionResult(handledCase)
    }

    /**
     * Login User to server
     *
     * @param loginRequest An Login Request created by user.
     * @return Returns ExecutionResult with Server-Provided token if succeeds.
     */
    fun loginUser(loginRequest: UserLoginRequest): ExecutionResult<UserLoginResponse> {
        // Handled Case
        val handledCase: HashMap<Int, (Response<UserLoginResponse>) -> ExecutionResult<UserLoginResponse>> = hashMapOf(
            HttpURLConnection.HTTP_OK to {
                ExecutionResult(ResultType.Success, value = it.body(), message = "")
            },
            HttpURLConnection.HTTP_FORBIDDEN to {
                ExecutionResult(ResultType.Forbidden, value = null, message = getErrorMessage(it))
            }
        )

        return serverRestApi.loginUser(loginRequest).getExecutionResult(handledCase)
    }

    /**
     * Get Server-Communication Result, in custom Execution Result
     * It will automatically handle with given 'handled case' but, if not, it will return ExecutionResult with unknown type.
     * @param T Response Type
     * @param handledCase Handled Case that we are expecting
     * @return Execution Result.
     */
    private fun<T> Call<T>.getExecutionResult(handledCase: HashMap<Int, (Response<T>) -> ExecutionResult<T>>): ExecutionResult<T> {
        return runCatching {
            val response = this.execute()
            handleCommunication(handledCase, response)
        }.getOrElse {
            ExecutionResult(ResultType.Connection, null, "Unknown Error Occurred!")
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

    /**
     * Get Error Message From Response.
     * May return server-side sent error message, or it will create unknown-default error message.
     *
     * @param T Response Type
     * @param response Response itself
     * @return Error Message.
     */
    private fun<T> getErrorMessage(response: Response<T>): String {
        return runCatching {
            objectMapper.readValue<ErrorResponseModel>(response.errorBody().toString()).message
        }.getOrElse {
            "Unknown Error Occurred!"
        }
    }
}