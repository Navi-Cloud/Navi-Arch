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
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.utils.PagerCacheUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PagerViewModel @Inject constructor(): ViewModel() {

    // Need to re-create pagerCacheUtils every time, because uploading activity might cause cache collision.
    private val pagerCacheUtils: PagerCacheUtils = PagerCacheUtils()

    // Set of pages for preventing same page is spammed.
    private val pageSet: MutableSet<String> = mutableSetOf()

    // Server Management
    private val serverManagement: ServerInterface = ServerManagement.getServerManagement()

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

    // The data we are going to share with view[MainActivity]
    val livePagerData: MutableLiveData<MutableList<FileAdapter>> = MutableLiveData()

    // Error Data in case of server connection issues
    val liveErrorData: MutableLiveData<Throwable> = MutableLiveData()

    // Current Page List
    val pageList: MutableList<FileAdapter> = mutableListOf()

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
        pageList[pageNum].fileList = sortList(pageList[pageNum].fileList)

        // Re-Enable cache
        pagerCacheUtils.createCache(targetTokenEntry, pageList[pageNum])

        // Notify Live Pager Data
        livePagerData.value = pageList
    }

    // Create page
    fun createInitialRootPage() {
        // Network on other thread
        innerExplorePage(
            FileData(
                userId = "",
                fileName = "/",
                fileType = "Folder",
                token = "rootToken",
                prevToken = "prevToken"
            ),
            true
        )
    }

    fun createInitialPage(fileData: FileData) {
        if(fileData.fileType == FileType.Folder.toString()) {
            innerExplorePage(fileData, false)
        } else {
            // If selected file is FILE, then get parent folder
            var parentFileData: FileData? = null

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    runCatching {
                        serverManagement.findFolderFromToken(
                            token = fileData.prevToken
                        )
                    }.onFailure {
                        parentFileData = null
                    }.onSuccess {
                        parentFileData = it
                    }
                }
                if(parentFileData != null){
                    innerExplorePage(parentFileData!!, false)
                } else {
                    // Alternative ...
                    createInitialRootPage()
                }
            }
        }
    }

    // Create Additional Page
    fun explorePage(nextFolder: FileData, requestedPageNumber: Int, cleanUp: Boolean = false) {
        // Invalidate all cache, set, pageList to cleanup[New-Fetch]
        cleanUpListCache(nextFolder, cleanUp)

        // Remove preceding pages if required.
        removePrecedingPages(requestedPageNumber)

        // Request Page - if we have page on cache, use it, or we request from server.
        requestPageWithCache(nextFolder)
    }

    // Update Cache Contents
    private fun updateCacheContents(fileAdapter: FileAdapter, targetToken: String, addToCache: Boolean) {
        // Update Cache - Always add cache when requesting root
        if (addToCache) {
            // Invalidate cache first
            pagerCacheUtils.invalidateCache(targetToken)

            // Create Cache
            pagerCacheUtils.createCache(targetToken, fileAdapter)
        }
    }

    private fun updatePageAndNotify(
        fileAdapter: FileAdapter,
        targetToken: String,
        addToCache: Boolean = false
    ) {
        // For Non-Automatic
        pageSet.add(targetToken)

        // Update cache if addToCache is true.
        updateCacheContents(fileAdapter, targetToken, addToCache)

        // Add to pageList
        pageList.add(fileAdapter)

        // Notify live pager data
        livePagerData.value = pageList
    }

    private fun cleanUpListCache(nextFolder: FileData, cleanUp: Boolean) {
        if (cleanUp) {
            pageSet.remove(nextFolder.token)
            pagerCacheUtils.invalidateCache(nextFolder.token)
            pageList.removeIf {
                it.currentFolder.token == nextFolder.token
            }
        }
    }

    private fun requestPageWithCache(nextFolder: FileData) {
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
        var throwable: Throwable? = null
        viewModelScope.launch {
            lateinit var sortedData: List<FileData>
            withContext(Dispatchers.IO) {
                // If is root, fetch rootToken first
                if (isRoot) {
                    nextFolder.token = runCatching {
                        serverManagement.getRootToken().rootToken
                    }.getOrElse {
                        throwable = it
                        return@withContext
                    }
                }

                // Get Data from server, and apply sort
                val tmpList: List<FileData> = runCatching {
                    serverManagement.getInsideFiles(nextFolder.token)
                }.getOrElse {
                    throwable = it
                    return@withContext
                }
                sortedData = sortList(tmpList)
            }

            // Check for error
            if (throwable != null) {
                liveErrorData.value = throwable
                return@launch
            }

            // So in main thread..
            val fileAdapter: FileAdapter = FileAdapter(
                onClick = recyclerOnClickListener,
                onLongClick = recyclerOnLongClickListener,
                fileList = sortedData,
                pageNumber = pageList.size + 1,
                currentFolder = nextFolder
            )
            updatePageAndNotify(fileAdapter, nextFolder.token, true)
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