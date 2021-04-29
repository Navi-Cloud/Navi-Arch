package com.kangdroid.navi_arch.server

import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import okhttp3.MultipartBody

interface ServerInterface {
    fun initWholeServerClient(): Boolean
    fun getRootToken(): RootTokenResponseDto
    fun getInsideFiles(requestToken: String): List<FileData>
    fun upload(Param: HashMap<String, Any>, file: MultipartBody.Part): String
    fun download(token: String): DownloadResponse
}