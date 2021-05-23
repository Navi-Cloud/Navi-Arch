package com.kangdroid.navi_arch.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import com.kangdroid.navi_arch.server.ServerInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun clearErrorData() {
        loginErrorData.value = null
    }

    fun login(userId: String, userPassword: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.loginUser(
                        LoginRequest(
                            userId = userId,
                            userPassword = userPassword
                        )
                    )
                }.onFailure {
                    onLoginFailed(it)
                }.onSuccess {
                    onLoginSucceed()
                }
            }
        }
    }

    private suspend fun onLoginSucceed() {
        withContext(Dispatchers.Main) {
            requestMainPage()
        }
    }

    private suspend fun onLoginFailed(throwable: Throwable) {
        withContext(Dispatchers.Main) {
            loginErrorData.value = throwable
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