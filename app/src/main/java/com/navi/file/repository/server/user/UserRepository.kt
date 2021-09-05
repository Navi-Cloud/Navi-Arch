package com.navi.file.repository.server.user

import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.factory.NaviRetrofitFactory
import com.navi.file.repository.server.factory.ServerRepositoryBase
import okhttp3.ResponseBody
import retrofit2.*
import java.net.HttpURLConnection

object UserRepository : ServerRepositoryBase() {

    // Create ApiInterface
    private val serverRestUserApi: UserApi = NaviRetrofitFactory.baseRetrofit.create()

    /**
     * Register user to server.
     *
     * @param userRegisterRequest User Register Request, containing some id, password, etc.
     * @return ExecutionResult,
     * Containing Result Type. But this does not return any data object, but only for checking server communication succeed.
     */
    fun registerUser(userRegisterRequest: UserRegisterRequest): ExecutionResult<ResponseBody> {
        // Handled Case
        val handledCase: HashMap<Int, (Response<ResponseBody>) -> ExecutionResult<ResponseBody>> =
            hashMapOf(
                HttpURLConnection.HTTP_OK to {
                    ExecutionResult(ResultType.Success, value = null, message = "")
                },
                HttpURLConnection.HTTP_CONFLICT to {
                    ExecutionResult(
                        ResultType.Conflict,
                        value = null,
                        message = getErrorMessage(it)
                    )
                }
            )

        return serverRestUserApi.registerUser(userRegisterRequest).getExecutionResult(handledCase)
    }

    /**
     * Login User to server
     *
     * @param loginRequest An Login Request created by user.
     * @return Returns ExecutionResult with Server-Provided token if succeeds.
     */
    fun loginUser(loginRequest: UserLoginRequest): ExecutionResult<UserLoginResponse> {
        // Handled Case
        val handledCase: HashMap<Int, (Response<UserLoginResponse>) -> ExecutionResult<UserLoginResponse>> =
            hashMapOf(
                HttpURLConnection.HTTP_OK to {
                    ExecutionResult(ResultType.Success, value = it.body(), message = "")
                },
                HttpURLConnection.HTTP_FORBIDDEN to {
                    ExecutionResult(
                        ResultType.Forbidden,
                        value = null,
                        message = getErrorMessage(it)
                    )
                }
            )

        return serverRestUserApi.loginUser(loginRequest).getExecutionResult(handledCase)
    }
}