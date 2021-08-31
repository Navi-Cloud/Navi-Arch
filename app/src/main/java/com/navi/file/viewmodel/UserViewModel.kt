package com.navi.file.viewmodel

import androidx.lifecycle.MutableLiveData
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.repository.server.ExecutionResult
import com.navi.file.repository.server.ServerRepository
import okhttp3.ResponseBody

class UserViewModel(
    private val serverRepository: ServerRepository,
    dispatcherInfo: DispatcherInfo = DispatcherInfo()
): ViewModelExtension(dispatcherInfo) {
    // Register Result
    var registerResult: MutableLiveData<ExecutionResult<ResponseBody>> = MutableLiveData()

    // Login Result
    var loginUser: MutableLiveData<ExecutionResult<UserLoginResponse>> = MutableLiveData()

    /**
     * Request User Registration to serverRepository.
     * Since all input validation is held on UI, so we just handle communication.
     *
     * @param userRegisterRequest User Register Request Model
     */
    fun requestUserRegister(userRegisterRequest: UserRegisterRequest) {
        dispatchIo {
            registerResult.postValue(serverRepository.registerUser(userRegisterRequest))
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
            loginUser.postValue(serverRepository.loginUser(userLoginRequest))
        }
    }
}