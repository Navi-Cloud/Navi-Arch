package com.kangdroid.navi_arch.fileView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.server.ServerManagement
import kotlinx.coroutines.*

class FileViewModel : ViewModel() {

    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val fileList: MutableList<FileData> = mutableListOf()
    val liveFileData: MutableLiveData<List<FileData>> = MutableLiveData()

    init {
        ServerManagement.initServerCommunication()
    }

    fun exploreData(toExploreToken: String) {
        coroutineScope.launch {
            val exploredData: List<FileData> = ServerManagement.getInsideFiles(toExploreToken)
            withContext(Dispatchers.Main) {
                liveFileData.value = exploredData
            }
        }
    }

    fun exploreRootData() {
        coroutineScope.launch {
            val rootToken: String = ServerManagement.getRootToken()
            val exploredData: List<FileData> = ServerManagement.getInsideFiles(rootToken)
            withContext(Dispatchers.Main) {
                liveFileData.value = exploredData
            }
        }
    }
}