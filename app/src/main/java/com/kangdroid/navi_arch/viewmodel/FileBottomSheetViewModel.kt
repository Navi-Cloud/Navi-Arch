package com.kangdroid.navi_arch.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class FileBottomSheetViewModel @Inject constructor(
    private val serverManagement: ServerInterface
): ViewModel() {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    // TODO: Dismiss Dialog when download is done.
    fun downloadFile(targetToken: String) {
        coroutineScope.launch {
            val downloadedResponse: DownloadResponse = serverManagement.download(targetToken)

            withContext(Dispatchers.Main) {
                saveDownloadedFile(downloadedResponse)
            }
        }
    }

    private fun saveDownloadedFile(downloadResponse: DownloadResponse) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            throw IllegalStateException("Cannot save file ${downloadResponse.fileName}. Perhaps device does not have any SDCard?")
        }

        val saveBaseDirectory: String = Environment.getExternalStorageDirectory().toString() + "/Download"

        // Open a file object for target
        val targetFile: File = File(saveBaseDirectory, downloadResponse.fileName)

        runCatching {
            // Buffer Size: 4086
            val fileReadingBuffer: ByteArray = ByteArray(4096)

            // Read File from downloaded content file[TMP]
            val inputStream: InputStream = downloadResponse.fileContents.byteStream()

            // Open an output stream for saving it
            val outputStream: FileOutputStream = FileOutputStream(targetFile)

            // Read it
            while (true) {
                val readByte: Int = inputStream.read(fileReadingBuffer)

                // Yup, done reading it
                if (readByte == -1) {
                    break
                }

                // Write read strings
                outputStream.write(fileReadingBuffer, 0, readByte)
            }

            // Flush outputStream
            outputStream.flush()
        }.onFailure {
            Log.d(this::class.java.simpleName, "Failed to save file to SDCard!")
            throw it
        }.onSuccess {
            Log.d(this::class.java.simpleName, "Successfully saved file")
        }
    }
}