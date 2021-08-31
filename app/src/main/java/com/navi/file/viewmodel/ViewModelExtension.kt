package com.navi.file.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class DispatcherInfo(
    val uiDispatcher: CoroutineDispatcher = Dispatchers.Main,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
)

abstract class ViewModelExtension(
    private val dispatcherInfo: DispatcherInfo
): ViewModel() {

    /**
     * Extension function that viewmodel can do ui jobs.
     *
     * @param block function to execute[lambda expression]
     * @return Job
     */
    fun ViewModel.dispatchUi(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(dispatcherInfo.uiDispatcher) {
            block()
        }
    }

    /**
     * Extension function that viewmodel can do IO jobs.
     *
     * @param block function to execute[lambda expression]
     * @return Job
     */
    fun ViewModel.dispatchIo(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(dispatcherInfo.ioDispatcher) {
            block()
        }
    }

    /**
     * Extension function that viewmodel can do background jobs.
     *
     * @param block function to execute[lambda expression]
     * @return Job
     */
    fun ViewModel.dispatchBackground(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(dispatcherInfo.backgroundDispatcher) {
            block()
        }
    }
}