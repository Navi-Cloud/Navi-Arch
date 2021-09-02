package com.navi.file.viewmodel

import androidx.lifecycle.MutableLiveData
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.user.UserRepository
import okhttp3.ResponseBody

class UserViewModel constructor(
    private val userRepository: UserRepository,
    dispatcherInfo: DispatcherInfo = DispatcherInfo()
): ViewModelExtension(dispatcherInfo) {
    // Register Result
    val registerResult: MutableLiveData<ExecutionResult<ResponseBody>> = MutableLiveData()

    // Login Result
    val loginUser: MutableLiveData<ExecutionResult<UserLoginResponse>> = MutableLiveData()

    /**
     * Request User Registration to serverRepository.
     * Since all input validation is held on UI, so we just handle communication.
     *
     * @param userRegisterRequest User Register Request Model
     */
    fun requestUserRegister(userRegisterRequest: UserRegisterRequest) {
        if (!userRegisterRequest.validateModel()) {
            // Valid Failed
            registerResult.postValue(
                ExecutionResult(
                    resultType = ResultType.ModelValidateFailed,
                    value = null,
                    message = "Input Email should be valid email, and password must contains special character, and its length should be more then 8."
                )
            )
        } else {
            // Valid. Do Dispatch.
            dispatchIo {
                registerResult.postValue(userRepository.registerUser(userRegisterRequest))
            }
        }
    }

    /**
     * Request User Login to serverRepository.
     * Since all input validation and after-handler is on UI, so we just handle communication.
     *
     * @param userLoginRequest
     */
    fun requestUserLogin(userLoginRequest: UserLoginRequest) {
        dispatchIo {
            loginUser.postValue(userRepository.loginUser(userLoginRequest))
        }
    }
}