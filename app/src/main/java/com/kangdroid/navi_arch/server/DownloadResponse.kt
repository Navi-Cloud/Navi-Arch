package com.kangdroid.navi_arch.server

import okhttp3.ResponseBody

data class DownloadResponse(
    var fileName: String,
    var fileContents: ResponseBody
)