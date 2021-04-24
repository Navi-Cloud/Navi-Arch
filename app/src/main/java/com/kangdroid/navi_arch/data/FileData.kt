package com.kangdroid.navi_arch.data

import java.text.SimpleDateFormat
import java.util.*

data class FileData(
    var id: Long = 0,
    var fileName: String,
    var fileType: String,
    var token: String,
    var lastModifiedTime: Long
) {

    fun getBriefName(): String {
        val toSplit: Char = if (this.fileName.contains('\\')) {
            '\\'
        } else {
            '/'
        }

        val listToken: List<String> = this.fileName.split(toSplit)
        return listToken[listToken.size - 1]
    }

    fun getFormattedDate(): String = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    ).format(Date(this.lastModifiedTime))
}

enum class FileType {
    Folder, File
}