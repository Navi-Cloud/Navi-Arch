package com.kangdroid.navi_arch.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kangdroid.navi_arch.data.dto.internal.ExecutionInformation
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MenuBottomSheetViewModel @Inject constructor() : ViewModel() {
    private val serverManagement: ServerManagement = ServerManagement.getServerManagement()

    var createFolderResult: MutableLiveData<ExecutionInformation<Any>> = MutableLiveData()

    fun createFolder(createFolderRequestDTO: CreateFolderRequestDTO) {
        Log.d(this::class.java.simpleName, "CreateFolder called")
        viewModelScope.launch {
            Log.d(this::class.java.simpleName, "Launch Called")
            withContext(Dispatchers.IO) {
                runCatching {
                    Log.d(this::class.java.simpleName, "RunCatching IO Called")
                    serverManagement.createFolder(createFolderRequestDTO)
                }.onSuccess {
                    createFolderResult.postValue(ExecutionInformation(true, null, null))
                }.onFailure {
                    createFolderResult.postValue(ExecutionInformation(false, null, it))
                }
            }
        }
    }
}