package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.utils.NaviFileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.Any
import kotlin.String
import kotlin.Unit
import kotlin.getValue
import kotlin.lazy
import kotlin.run
import kotlin.with

@HiltViewModel
class UploadingViewModel @Inject constructor(
        private val rawApplication: Application,
        private val serverManagement: ServerInterface
    ) : AndroidViewModel(rawApplication) {

    private val logTag: String = this::class.java.simpleName

    private lateinit var bufferedReader: BufferedReader

    // Application Context
    private val context: Context by lazy {
        rawApplication.applicationContext
    }

    // Context's Content Resolver[From MainActivity]
    private val contentResolver: ContentResolver by lazy {
        rawApplication.applicationContext.contentResolver
    }

    // File TMP URI in String
    private var fileUri: String = ""

    fun createFileUri(uri: Uri) {
        Log.d(logTag, "Uri input: $uri")

        // Open file from URI
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: run {
            Log.e(logTag, "Cannot initiate Input Stream!")
            return
        }

        // Set bufferedReader
        bufferedReader = BufferedReader(InputStreamReader(inputStream))
        fileUri = NaviFileUtils.getPathFromUri(context, uri).toString()

        if (fileUri == NaviFileUtils.ERROR_GETTING_FILENAME) {
            fileUri = ""
        }
    }

    fun upload(uploadPath: String, actionAfterUpload: (() -> Unit)) {
        Log.d(logTag, "Upload Path: $uploadPath")

        val filename : String = fileUri.substring(fileUri.lastIndexOf("/")+1)

        val file : File = File(fileUri)

        Log.d(logTag,"File Name: $filename")


        val requestBody : RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile",filename,requestBody)
        val param : HashMap<String,Any> = HashMap()
        with(param){
            put("uploadPath", uploadPath)
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                serverManagement.upload(param, uploadFile)
            }
            withContext(Dispatchers.Main) {
                actionAfterUpload()
            }
        }
    }
}