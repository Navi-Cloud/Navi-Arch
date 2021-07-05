package com.kangdroid.navi_arch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun createFolder(createFolderRequestDTO: CreateFolderRequestDTO, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    serverManagement.createFolder(createFolderRequestDTO)
                }.onSuccess {
                    handleCause(onSuccess, "")
                }.onFailure {
                    handleCause(onFailure, it)
                }
            }
        }
    }

    private suspend fun<T> handleCause(lambdaExecute: (T) -> Unit, capturedValue: T) {
        withContext(Dispatchers.Main) {
            lambdaExecute(capturedValue)
        }
    }
}