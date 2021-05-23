package com.kangdroid.navi_arch.server

import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import okhttp3.MultipartBody

interface ServerInterface {
    fun initWholeServerClient(): Boolean
    fun getRootToken(): RootTokenResponseDto
    fun getInsideFiles(requestToken: String): List<FileData>
    fun upload(Param: HashMap<String, Any>, file: MultipartBody.Part): String
    fun download(token: String): DownloadResponse
    fun loginUser(userLoginRequest : LoginRequest): LoginResponse
    fun register(userRegisterRequest : RegisterRequest) : RegisterResponse
}