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

    // Cache Related - Only remove cache when upload method is defined.
    private val pageCache: HashMap<String, FileAdapter> = HashMap()

    val livePagerData: MutableLiveData<MutableList<FileAdapter>> = MutableLiveData()

    // Inner Recycler View onClickListener
    private val recyclerOnClickListener: ((FileData, Int) -> Unit) = { it, pageNumber ->
        Log.d(this::class.java.simpleName, "ViewModel Testing")
        Log.d(this::class.java.simpleName, "Token: ${it.token}")

        // Only Explore Folder pages
        if (it.fileType == FileType.Folder.toString()) {
            explorePage(it, pageNumber)
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
                val fileAdapter: FileAdapter = FileAdapter(
                    recyclerOnClickListener,
                    exploredData,
                    pageList.size + 1,
                    FileData (fileName = "/", fileType = "Folder", lastModifiedTime = System.currentTimeMillis(), token = rootToken)
                )

                // For Non-Automatic
                pageSet.add(rootToken)

                // Update Cache - Always add cache when requesting root
                pageCache[rootToken] = fileAdapter

                // Add to pageList
                pageList.add(fileAdapter)
                livePagerData.value = pageList
            }
        }
    }

    // Create Additional Page
    private fun explorePage(nextFolder: FileData, requestedPageNumber: Int) {
        // Remove preceding pages if required.
        if (pageList.size > requestedPageNumber) {
            removePrecedingPages(requestedPageNumber)
        }

        // Find whether token is on page.
        if (!pageSet.contains(nextFolder.token)) {
            // Check for cache
            if (pageCache.contains(nextFolder.token)) {
                Log.d(this::class.java.simpleName, "Using Cache for ${nextFolder.token}")
                pageSet.add(nextFolder.token)
                pageList.add(pageCache[nextFolder.token]!!)
                livePagerData.value = pageList
            } else {
                // Not on cache
                coroutineScope.launch {
                    val exploredData: List<FileData> = ServerManagement.getInsideFiles(nextFolder.token)
                    withContext(Dispatchers.Main) {
                        val fileAdapter: FileAdapter = FileAdapter(
                            recyclerOnClickListener,
                            exploredData,
                            pageList.size + 1,
                            nextFolder
                        )

                        // Add to Main List
                        pageList.add(fileAdapter)

                        // Add to Set
                        pageSet.add(nextFolder.token)

                        // Add to Cache
                        pageCache[nextFolder.token] = fileAdapter

                        // Notify MainActivity
                        livePagerData.value = pageList
                    }
                }
            }
        }
    }

    private fun removePrecedingPages(requestedPageNumber: Int) {
        val toCount: Int = (pageList.size) - (requestedPageNumber)

        for (i in 0 until toCount) {
            // Last Page
            val lastPage: FileAdapter = pageList.last()
            Log.d(this::class.java.simpleName, "Removing: ${lastPage.pageNumber}")
            Log.d(this::class.java.simpleName, "Removed Token: ${lastPage.currentFolder.token}")
            pageSet.removeIf {
                it == lastPage.currentFolder.token
            }
            pageList.removeLast()
        }
    }
}