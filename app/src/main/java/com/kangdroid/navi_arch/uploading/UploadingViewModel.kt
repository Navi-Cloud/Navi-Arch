package com.kangdroid.navi_arch.uploading

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.utils.NaviFileUtils
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class UploadingViewModel(private val rawApplication: Application) : AndroidViewModel(rawApplication) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

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
        fileUri = NaviFileUtils.getPathFromUri(context, uri)

        if (fileUri == NaviFileUtils.ERROR_GETTING_FILENAME) {
            fileUri = ""
        }
    }

    fun upload(uploadPath: String, actionAfterUpload: (() -> Unit)) {
        Log.d(logTag, "Upload Path: $uploadPath")

        val filename : String = fileUri.substring(fileUri.lastIndexOf("/")+1)
        Log.d(logTag, "File Name: $filename")

        val file : File = File.createTempFile(filename,null, context.cacheDir)

        var strReader: String? = null
        while (true) {
            strReader = bufferedReader.readLine()
            if (strReader == null) break
            file.appendText(strReader+"\n")
        }

        Log.e(logTag,"File Contents: ${file.readText()}")
        Log.e(logTag,"File Name: $filename")

        val requestBody : RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile",filename,requestBody)
        val param : HashMap<String,Any> = HashMap()
        with(param){
            put("uploadPath", uploadPath)
        }

        coroutineScope.launch {
            ServerManagement.upload(param, uploadFile)
            withContext(Dispatchers.Main) {
                actionAfterUpload()
            }
        }
    }
}