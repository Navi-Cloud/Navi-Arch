package com.kangdroid.navi_arch.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    // Search Result and Live data
    private var searchResultList: List<FileData> = listOf()
    val searchResultLiveData: MutableLiveData<List<FileData>> = MutableLiveData()

    // Error Data in case of server connection issues
    val liveErrorData: MutableLiveData<Throwable> = MutableLiveData()

    // Server Management
    private val serverManagement: ServerInterface = ServerManagement.getServerManagement()

    // Get Search Result
    fun search(query: String,
               mode: FileSortingMode = FileSortingMode.TypedName,
               isReversed: Boolean = false) {

        var searchThrow : Throwable?= null

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.searchFile(
                        searchParam = query
                    )
                }.onFailure {
                    searchThrow = it
                }.onSuccess {
                    searchResultList = it
                }
            }

            // Check Error
            if (searchThrow != null) {
                liveErrorData.value = searchThrow
                return@launch
            }

            sort(mode, isReversed)
        }
    }

    // Sort
    fun sort(mode: FileSortingMode, isReversed: Boolean) {
        // Sort with Comparator
        val sortedList: List<FileData> = if (isReversed) {
            searchResultList.sortedWith(mode).asReversed()
        } else {
            searchResultList.sortedWith(mode)
        }

        // Notify Live Data
        searchResultLiveData.value = sortedList
    }
}