package com.navi.file.viewmodel

import androidx.lifecycle.MutableLiveData
import com.navi.file.helper.FormValidator.validateModel
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.user.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository,
    dispatcherInfo: DispatcherInfo = DispatcherInfo()
): ViewModelExtension(dispatcherInfo) {
    // Login Result
    val loginUser: MutableLiveData<ExecutionResult<UserLoginResponse>> = MutableLiveData()

    /**
     * Request User Login to serverRepository.
     * Since all input validation and after-handler is on UI, so we just handle communication.
     *
     * @param userLoginRequest
     */
    fun requestUserLogin(email: String, password: String) {
        if (!validateModel(email, password)) {
            loginUser.value = ExecutionResult(
                resultType = ResultType.ModelValidateFailed,
                value = null,
                message = "Input Email should be valid email, and password must contains special character, and its length should be more then 8."
            )
        } else {
            dispatchIo {
                loginUser.postValue(
                    userRepository.loginUser(
                        UserLoginRequest(email, password)
                    )
                )
            }
        }
    }
}