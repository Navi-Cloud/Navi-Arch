package com.kangdroid.navi_arch.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.internal.ExecutionInformation
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import javax.inject.Inject

@HiltViewModel
class FileBottomSheetViewModel @Inject constructor(): ViewModel() {

    private val serverManagement: ServerInterface = ServerManagement.getServerManagement()

    var removeFileExecutionResult: MutableLiveData<ExecutionInformation<Any>> = MutableLiveData()
    var downloadFileExecutionResult: MutableLiveData<ExecutionInformation<String>> = MutableLiveData()

    // TODO: Dismiss Dialog when download is done.
    fun downloadFile(targetToken: String, prevToken: String) {
        Log.d(this::class.java.simpleName, "Accessing downloadFile")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.download(targetToken, prevToken)
                }.onSuccess {
                    saveDownloadedFile(it)
                    downloadFileExecutionResult.postValue(ExecutionInformation(true, it.fileName, null))
                }.onFailure {
                    Log.e(this::class.java.simpleName, it.stackTraceToString())
                    downloadFileExecutionResult.postValue(ExecutionInformation(false, null, it))
                }
            }
        }
    }

    fun removeFile(prevToken: String, targetToken: String) {
        Log.d(this::class.java.simpleName, "Removing Target")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.removeFile(prevToken, targetToken)
                }.onSuccess {
                    removeFileExecutionResult.postValue(ExecutionInformation(true, null, null))
                }.onFailure {
                    Log.e(this::class.java.simpleName, it.stackTraceToString())
                    removeFileExecutionResult.postValue(ExecutionInformation(false, null, it))
                }
            }
        }
    }

    private fun saveDownloadedFile(downloadResponse: DownloadResponse) {
        Log.d(this::class.java.simpleName, "Accessing saveDownloadedFile")
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            throw IllegalStateException("Cannot save file ${downloadResponse.fileName}. Perhaps device does not have any SDCard?")
        }

        val saveBaseDirectory: String = Environment.getExternalStorageDirectory().toString() + "/Download"

        // Open a file object for target
        val targetFile: File = File(saveBaseDirectory, downloadResponse.fileName)

        // Read File from downloaded content file[TMP]
        val inputStream: InputStream = downloadResponse.fileContents.byteStream()

        runCatching {
            Files.copy(inputStream, targetFile.toPath())
        }.onSuccess {
            Log.d(this::class.java.simpleName, "Successfully saved file")
        }.onFailure {
            Log.d(this::class.java.simpleName, "Failed to save file to SDCard!")
            throw it
        }
    }
}