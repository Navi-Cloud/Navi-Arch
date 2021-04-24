package com.kangdroid.navi_arch.fileView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.server.ServerManagement

class FileViewModel(application: Application) : AndroidViewModel(application) {

    private val fileList: MutableList<FileData> = mutableListOf()
    val liveFileData: MutableLiveData<List<FileData>> = MutableLiveData()

    init {
        ServerManagement.initServerCommunication()
    }

    fun exploreData(toExploreToken: String) {
        val exploredData: List<FileData> = ServerManagement.getInsideFiles(toExploreToken)
        liveFileData.value = exploredData
    }
}