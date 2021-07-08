package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import com.kangdroid.navi_arch.server.ServerManagement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedReader

class MakeFolderViewModel (application: Application) : AndroidViewModel(application){
    private val logTag: String = this::class.java.simpleName

    private lateinit var bufferedReader: BufferedReader

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement()

    // Application Context
    private val context: Context by lazy {
        application.applicationContext
    }

    // Context's Content Resolver[From MainActivity]
    private val contentResolver: ContentResolver by lazy {
        application.applicationContext.contentResolver
    }

    fun makeFolder(
        createFolderRequest: CreateFolderRequestDTO, actionAfterUpload: (() -> Unit)
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                serverManagement.makeFolder(createFolderRequest)
            }
            withContext(Dispatchers.Main) {
                actionAfterUpload()
            }
        }
    }
}