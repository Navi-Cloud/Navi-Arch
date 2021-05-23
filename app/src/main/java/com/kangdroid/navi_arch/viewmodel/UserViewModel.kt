package com.kangdroid.navi_arch.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import com.kangdroid.navi_arch.server.ServerInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import javax.inject.Inject

enum class PageRequest {
    REQUEST_LOGIN, REQUEST_REGISTER, REQUEST_MAIN
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val serverManagement: ServerInterface
) : ViewModel() {

    // TAG
    private val logTag: String = this::class.java.simpleName

    // Live data for login error
    val loginErrorData: MutableLiveData<Throwable> = MutableLiveData()

    // Register Request
    val pageRequest: MutableLiveData<PageRequest> = MutableLiveData()

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

    fun clearErrorData() {
        loginErrorData.value = null
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
                withContext(Dispatchers.Main) {
                    requestMainPage()
                }
            }
            if(loginthrow != null){
                withContext(Dispatchers.Main){
                    loginError(loginthrow!!)
                }
            }
        }
    }

    fun register(
        userId: String,
        userName: String,
        userEmail: String,
        userPassword: String
    ) {
        // TODO id/email check
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val response: RegisterResponse = serverManagement.register(
                    RegisterRequest(
                        userId = userId,
                        userName = userName,
                        userEmail = userEmail,
                        userPassword = userPassword
                    )
                )
            }
            withContext(Dispatchers.Main) {
                requestLoginPage()
            }
        }
    }
}