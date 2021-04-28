package com.kangdroid.navi_arch.bottom

import androidx.lifecycle.ViewModel
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileBottomSheetViewModel @Inject constructor(
    private val serverManagement: ServerManagement
): ViewModel() {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    // TODO: Dismiss Dialog when download is done.
    fun downloadFile(targetToken: String) {
        coroutineScope.launch {
            serverManagement.download(targetToken)
        }
    }
}