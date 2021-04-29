package com.kangdroid.navi_arch.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.utils.PagerCacheUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PagerViewModel @Inject constructor(
        private val serverManagement: ServerInterface,
        private val pagerCacheUtils: PagerCacheUtils
    ): ViewModel() {

    // Current Page List
    private val pageList: MutableList<FileAdapter> = mutableListOf()

    // Set of pages for preventing same page is spammed.
    private val pageSet: MutableSet<String> = mutableSetOf()

    // The data we are going to share with view[MainActivity]
    val livePagerData: MutableLiveData<MutableList<FileAdapter>> = MutableLiveData()

    // Server Management

    // For Sorting
    private var currentSortMode: FileSortingMode = FileSortingMode.TypedName
    private var isReversed: Boolean = false

    // Inner Recycler View onClickListener
    private val recyclerOnClickListener: ((FileData, Int) -> Unit) = { it, pageNumber ->
        Log.d(this::class.java.simpleName, "ViewModel Testing")
        Log.d(this::class.java.simpleName, "Token: ${it.token}")

        // Only Explore Folder pages
        if (it.fileType == FileType.Folder.toString()) {
            explorePage(it, pageNumber)
        }
    }

    // Inner Recycler View onLongClickListener
    var recyclerOnLongClickListener: ((FileData) -> Boolean)? = null

    // Sort
    fun sort(mode: FileSortingMode, reverse: Boolean, pageNum: Int) {
        // Update
        currentSortMode = mode
        isReversed = reverse

        // Target Token
        val targetTokenEntry: String = pageList[pageNum].currentFolder.token

        // Invalidate current cache
        pagerCacheUtils.invalidateCache(targetTokenEntry)

        // Sort them with Comparator
        pageList[pageNum].fileList = if (reverse) {
            pageList[pageNum].fileList.sortedWith(mode).asReversed()
        } else {
            pageList[pageNum].fileList.sortedWith(mode)
        }

        // Re-Enable cache
        pagerCacheUtils.createCache(targetTokenEntry, pageList[pageNum])

        // Notify Live Pager Data
        livePagerData.value = pageList
    }

    private fun updatePageAndNotify(
        fileAdapter: FileAdapter,
        targetToken: String,
        addToCache: Boolean = false
    ) {
        // For Non-Automatic
        pageSet.add(targetToken)

        // Update Cache - Always add cache when requesting root
        if (addToCache) {
            // Invalidate cache first
            pagerCacheUtils.invalidateCache(targetToken)

            // Create Cache
            pagerCacheUtils.createCache(targetToken, fileAdapter)
        }

        // Add to pageList
        pageList.add(fileAdapter)

        // Notify live pager data
        livePagerData.value = pageList
    }

    // Create page
    fun createInitialRootPage() {
        // Network on other thread
        innerExplorePage(
            FileData(
                fileName = "/",
                fileType = "Folder",
                lastModifiedTime = System.currentTimeMillis(),
                token = "rootToken"
            ),
            true
        )
    }

    // Create Additional Page
    fun explorePage(nextFolder: FileData, requestedPageNumber: Int, cleanUp: Boolean = false) {
        // Invalidate all cache, set, pageList to cleanup[New-Fetch]
        if (cleanUp) {
            pageSet.remove(nextFolder.token)
            pagerCacheUtils.invalidateCache(nextFolder.token)
            pageList.removeIf {
                it.currentFolder.token == nextFolder.token
            }
        }

        // Remove preceding pages if required.
        removePrecedingPages(requestedPageNumber)

        // Find whether token is on page.
        if (!pageSet.contains(nextFolder.token)) {
            // Check for cache
            runCatching {
                pagerCacheUtils.getCacheContent(nextFolder.token)
            }.onSuccess {
                Log.d(this::class.java.simpleName, "Using Cache for ${nextFolder.token}")
                updatePageAndNotify(it, nextFolder.token, false)
            }.onFailure {
                // Not on cache
                Log.d(this::class.java.simpleName, it.stackTraceToString())
                Log.d(this::class.java.simpleName, "Reloading fetching folder data from server..")
                innerExplorePage(nextFolder)
            }
        }
    }

    private fun innerExplorePage(nextFolder: FileData, isRoot: Boolean = false) {
        viewModelScope.launch {
            lateinit var sortedData: List<FileData>
            withContext(Dispatchers.IO) {
                // If is root, fetch rootToken first
                if (isRoot) {
                    nextFolder.token = serverManagement.getRootToken().rootToken
                }

                // Get Data from server, and apply sort
                sortedData = sortList(serverManagement.getInsideFiles(nextFolder.token))
            }

            // So in main thread..
            withContext(Dispatchers.Main) {
                val fileAdapter: FileAdapter = FileAdapter(
                    recyclerOnClickListener,
                    recyclerOnLongClickListener,
                    sortedData,
                    pageList.size + 1,
                    nextFolder
                )
                updatePageAndNotify(fileAdapter, nextFolder.token, true)
            }
        }
    }

    private fun removePrecedingPages(requestedPageNumber: Int) {
        if (pageList.size > requestedPageNumber) {
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

    private fun sortList(fileList: List<FileData>): List<FileData> {
        // Get Data from server, and apply sort
        return if (isReversed) {
            fileList.sortedWith(currentSortMode).asReversed()
        } else {
            fileList.sortedWith(currentSortMode)
        }
    }
}