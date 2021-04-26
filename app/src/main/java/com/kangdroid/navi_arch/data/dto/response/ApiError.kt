package com.kangdroid.navi_arch.data.dto.response

data class ApiError (
    val message: String = "",
    var statusCode: String = "",
    var statusMessage: String = "",
)