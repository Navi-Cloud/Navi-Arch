package com.kangdroid.navi_arch.pager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.recyclerview.FileAdapter
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class PagerViewModel @Inject constructor(
        private val serverManagement: ServerManagement
    ): ViewModel() {

    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    // Current Page List
    private val pageList: MutableList<FileAdapter> = mutableListOf()

    // Set of pages for preventing same page is spammed.
    private val pageSet: MutableSet<String> = mutableSetOf()

    // Cache Related - Only remove cache when upload method is defined.
    private val pageCache: HashMap<String, FileAdapter> = HashMap()

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

    // Sort
    fun sort(mode: FileSortingMode, reverse: Boolean, pageNum: Int) {
        // Update Fields
        currentSortMode = mode
        isReversed = reverse


        // Invalidate current cache
        pageCache.remove(pageList[pageNum].currentFolder.token)

        // Sort them with Comparator
        pageList[pageNum].fileList = if (reverse) {
            pageList[pageNum].fileList.sortedWith(mode).asReversed()
        } else {
            pageList[pageNum].fileList.sortedWith(mode)
        }

        // Re-Enable cache
        pageCache[pageList[pageNum].currentFolder.token] = pageList[pageNum]

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
            pageCache[targetToken] = fileAdapter
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
            pageCache.remove(nextFolder.token)
            pageList.removeIf {
                it.currentFolder.token == nextFolder.token
            }
        }

        // Remove preceding pages if required.
        if (pageList.size > requestedPageNumber) {
            removePrecedingPages(requestedPageNumber)
        }

        // Find whether token is on page.
        if (!pageSet.contains(nextFolder.token)) {
            // Check for cache
            if (pageCache.contains(nextFolder.token)) {
                // Use Cache
                Log.d(this::class.java.simpleName, "Using Cache for ${nextFolder.token}")
                updatePageAndNotify(pageCache[nextFolder.token]!!, nextFolder.token, false)
            } else {
                // Not on cache
                innerExplorePage(nextFolder)
            }
        }
    }

    private fun innerExplorePage(nextFolder: FileData, isRoot: Boolean = false) {
        coroutineScope.launch {
            // If is root, fetch rootToken first
            if (isRoot) {
                nextFolder.token = serverManagement.getRootToken().rootToken
            }

            // Get Data from server, and apply sort
            val exploredData: List<FileData> = serverManagement.getInsideFiles(nextFolder.token)
            val sortedData: List<FileData> = if (isReversed) {
                exploredData.sortedWith(currentSortMode).asReversed()
            } else {
                exploredData.sortedWith(currentSortMode)
            }

            // So in main thread..
            withContext(Dispatchers.Main) {
                val fileAdapter: FileAdapter = FileAdapter(
                    recyclerOnClickListener,
                    sortedData,
                    pageList.size + 1,
                    nextFolder
                )
                updatePageAndNotify(fileAdapter, nextFolder.token, true)
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