package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.UserRegisterResponse
import com.kangdroid.navi_arch.server.ServerInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val rawApplication: Application,
    private val serverManagement: ServerInterface
) : AndroidViewModel(rawApplication) {

    // TAG
    private val logTag: String = this::class.java.simpleName

    // Application Context
    private val context: Context by lazy {
        rawApplication.applicationContext
    }

    fun login(userId : String,
              userPassword : String): String {
        var response: LoginResponse? = null
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                serverManagement.loginUser(
                    LoginRequest(
                        userId = userId,
                        userPassword = userPassword
                    )
                )
            }
        }
        return response?.userToken ?: ""
    }

    fun register(userId : String,
                 userName : String,
                 userEmail : String,
                 userPassword : String): Boolean {

        // TODO id/email check
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val response: UserRegisterResponse = serverManagement.register(
                    RegisterRequest(
                        userId = userId,
                        userName = userName,
                        userEmail = userEmail,
                        userPassword = userPassword
                    )
                )
            }
        }
        return true
    }
}