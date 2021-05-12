package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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

    // Live data for login error
    val liveErrorData: MutableLiveData<Throwable> = MutableLiveData()

    fun login(userId : String,
              userPassword : String,
              afterLoginSuccess: (()-> Unit)) {
        var throwable: Throwable? = null

        viewModelScope.launch {
            withContext(Dispatchers.IO){
                runCatching {
                    serverManagement.loginUser(
                        LoginRequest(
                            userId = userId,
                            userPassword = userPassword
                        )
                    )
                }.onFailure {
                    throwable = it
                }
            }
            withContext(Dispatchers.Main){
                liveErrorData.value = throwable
                afterLoginSuccess()
            }
        }
    }

    fun register(userId : String,
                 userName : String,
                 userEmail : String,
                 userPassword : String,
                 afterRegisterSuccess: (() -> Unit)) {
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
            withContext(Dispatchers.Main){
                afterRegisterSuccess()
            }
        }
    }
}