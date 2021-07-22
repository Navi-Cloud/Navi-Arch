package com.kangdroid.navi_arch.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class PageRequest {
    REQUEST_LOGIN, REQUEST_REGISTER, REQUEST_MAIN
}

@HiltViewModel
class UserViewModel @Inject constructor(): ViewModel() {

    // TAG
    private val logTag: String = this::class.java.simpleName

    // Live data for Login/Register error
    val loginErrorData: MutableLiveData<Throwable> = MutableLiveData()
    val registerErrorData: MutableLiveData<Throwable> = MutableLiveData()

    // Register Request
    val pageRequest: MutableLiveData<PageRequest> = MutableLiveData()

    private val serverManagement: ServerInterface = ServerManagement.getServerManagement()

    var loginresponse : LoginResponse ?= null

    // Request Register Page
    fun requestRegisterPage() {
        pageRequest.value = PageRequest.REQUEST_REGISTER
    }

    // Request Login Page
    fun requestLoginPage() {
        pageRequest.value = PageRequest.REQUEST_LOGIN
    }

    // Request Main Page
    private fun requestMainPage() {
        pageRequest.value = PageRequest.REQUEST_MAIN
    }

    fun loginError(throwable : Throwable){
        loginErrorData.value = throwable
    }

    fun registerError(throwable : Throwable){
        registerErrorData.value = throwable
    }

    fun clearLoginErrorData() {
        loginErrorData.value = null
    }

    fun clearRegisterErrorData() {
        registerErrorData.value = null
    }

    fun login(userId: String, userPassword: String) {
        viewModelScope.launch {

            var loginthrow : Throwable ?= null

            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.loginUser(
                        LoginRequest(
                            userId = userId,
                            userPassword = userPassword
                        )
                    )
                }.onFailure {
                    loginthrow = it
                }.onSuccess {
                    loginresponse = it
                }
            }

            if(loginresponse != null){
                requestMainPage()
            }
            if(loginthrow != null){
                loginError(loginthrow!!)
            }
        }
    }

    fun register(
        userId: String,
        userName: String,
        userEmail: String,
        userPassword: String
    ) {
        var registerResponse: RegisterResponse? = null
        var registerThrow : Throwable?= null

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.register(
                        RegisterRequest(
                            userId = userId,
                            userName = userName,
                            userEmail = userEmail,
                            userPassword = userPassword
                        )
                    )
                }.onFailure {
                    registerThrow = it
                }.onSuccess {
                    registerResponse = it
                }
            }
            if(registerResponse != null){
                requestLoginPage()
            }
            if (registerThrow != null){
                registerError(registerThrow!!)
            }
        }
    }
}