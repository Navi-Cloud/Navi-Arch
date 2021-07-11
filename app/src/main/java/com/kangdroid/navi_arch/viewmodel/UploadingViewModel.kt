package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.server.ServerManagement
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
        private val rawApplication: Application
    ) : AndroidViewModel(rawApplication) {

    private val logTag: String = this::class.java.simpleName

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement()

    // Application Context
    private val context: Context by lazy {
        rawApplication.applicationContext
    }

    // Context's Content Resolver[From MainActivity]
    private val contentResolver: ContentResolver by lazy {
        rawApplication.applicationContext.contentResolver
    }

    lateinit var fileContentArray: ByteArray
    lateinit var fileName: String
    lateinit var uploadFile : MultipartBody.Part

    fun getFileName(uri: Uri): String {
        var targetString: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                targetString = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                )
            }
            cursor?.close()
        }

        if (targetString == null) {
            val splitList: List<String> = uri.path?.split("/")!!
            targetString = splitList[splitList.size-1]
        }

        Log.d(this::class.java.simpleName, "Final file name: $targetString")
        return targetString
    }

    fun createFileUri(uri: Uri) {
        Log.d(logTag, "Uri input: $uri")

        // Open file from URI
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: run {
            Log.e(logTag, "Cannot initiate Input Stream!")
            return
        }

        fileContentArray = inputStream.readBytes()
        fileName = getFileName(uri)

        Log.d(logTag, "filecontentArray: $fileContentArray")
        Log.d(logTag, "fileName: $fileName")
    }

    fun upload(uploadPath: String, actionAfterUpload: (() -> Unit)) {
        Log.d(logTag, "Upload Path: $uploadPath")

        Log.d(logTag,"File Name: $fileName")

        // RequestBody.create(,file)
        val requestBody : RequestBody = RequestBody.create(
            contentType = "multipart/form-data".toMediaTypeOrNull(),
            content = fileContentArray
        )
        uploadFile = MultipartBody.Part.createFormData("uploadFile",fileName,requestBody)
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