package com.kangdroid.navi_arch.data.dto.internal

data class ExecutionInformation<T>(
    var isSucceed: Boolean,
    var value: T?,
    var error: Throwable?
)