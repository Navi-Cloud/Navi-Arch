package com.kangdroid.navi_arch.pager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.recyclerview.FileAdapter
import com.kangdroid.navi_arch.server.ServerManagement
import kotlinx.coroutines.*

class PagerViewModel: ViewModel() {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val pageList: MutableList<FileAdapter> = mutableListOf()
    val livePagerData: MutableLiveData<MutableList<FileAdapter>> = MutableLiveData()

    // Inner Recycler View onClickListener
    val recyclerOnClickListener: ((FileData) -> Unit) = {
        Log.d(this::class.java.simpleName, "ViewModel Testing")
        Log.d(this::class.java.simpleName, "Token: ${it.token}")
    }

    init {
        ServerManagement.initServerCommunication()
    }

    // Create page
    fun createInitialRootPage() {
        coroutineScope.launch {
            val rootToken: String = ServerManagement.getRootToken()
            val exploredData: List<FileData> = ServerManagement.getInsideFiles(rootToken)
            withContext(Dispatchers.Main) {
                pageList.add(FileAdapter(recyclerOnClickListener, exploredData))
                livePagerData.value = pageList
            }
        }
    }
}