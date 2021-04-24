package com.kangdroid.navi_arch.data

data class FileData(
    var id: Long = 0,
    var fileName: String,
    var fileType: String,
    var token: String,
    var lastModifiedTime: Long
)
