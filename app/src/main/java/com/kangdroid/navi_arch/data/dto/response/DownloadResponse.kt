package com.kangdroid.navi_arch.data.dto.response

import okhttp3.ResponseBody

data class DownloadResponse(
    var fileName: String,
    var fileContents: ResponseBody
)