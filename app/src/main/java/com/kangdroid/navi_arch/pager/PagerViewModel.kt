package com.kangdroid.navi_arch.pager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.recyclerview.FileAdapter
import com.kangdroid.navi_arch.server.ServerManagement
import kotlinx.coroutines.*

class PagerViewModel : ViewModel() {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val pageList: MutableList<FileAdapter> = mutableListOf()
    private val pageSet: MutableSet<String> = mutableSetOf()

    val livePagerData: MutableLiveData<MutableList<FileAdapter>> = MutableLiveData()

    // Inner Recycler View onClickListener
    private val recyclerOnClickListener: ((FileData, Int) -> Unit) = { it, pageNumber ->
        Log.d(this::class.java.simpleName, "ViewModel Testing")
        Log.d(this::class.java.simpleName, "Token: ${it.token}")

        // Only Explore Folder pages
        if (it.fileType == FileType.Folder.toString()) {
            explorePage(it.token, pageNumber)
        }
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
                pageSet.add(rootToken)
                pageList.add(
                    FileAdapter(
                        recyclerOnClickListener,
                        exploredData,
                        pageList.size + 1,
                        rootToken
                    )
                )
                livePagerData.value = pageList
            }
        }
    }

    // Create Additional Page
    private fun explorePage(exploreToken: String, requestedPageNumber: Int) {

        // Remove last pages
        // Aka if user is currently viewing /tmp/testing/whatever
        // and user requested to view /tmp/what, then we need to remove those /testing/whatever and add it.
        if (pageList.size > requestedPageNumber) {
            val toCount: Int = (pageList.size) - (requestedPageNumber)

            for (i in 0 until toCount) {
                // Last Page
                val lastPage: FileAdapter = pageList.last()
                Log.d(this::class.java.simpleName, "Removing: ${lastPage.pageNumber}")
                Log.d(this::class.java.simpleName, "Removed Token: ${lastPage.currentFolderToken}")
                pageSet.removeIf {
                    it == lastPage.currentFolderToken
                }
                pageList.removeLast()
            }
        }


        // Find whether token is on page.
        if (!pageSet.contains(exploreToken)) {
            coroutineScope.launch {
                val exploredData: List<FileData> = ServerManagement.getInsideFiles(exploreToken)
                withContext(Dispatchers.Main) {
                    pageList.add(
                        FileAdapter(
                            recyclerOnClickListener,
                            exploredData,
                            pageList.size + 1,
                            exploreToken
                        )
                    )
                    pageSet.add(exploreToken)
                    livePagerData.value = pageList
                }
            }
        }
    }
}