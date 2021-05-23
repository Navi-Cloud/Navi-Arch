package com.kangdroid.navi_arch.viewmodel

import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.*
import com.kangdroid.navi_arch.server.ServerInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.ResponseBody

class FakeServerManagement : ServerInterface {
    override fun initWholeServerClient(): Boolean = true

    override fun getRootToken(): RootTokenResponseDto = RootTokenResponseDto(
        rootToken = "TestingRootToken"
    )

    override fun getInsideFiles(requestToken: String): List<FileData> = listOf(
        FileData(
            id = 10,
            fileName = "/tmp/a.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/a.txt.token",
            lastModifiedTime = System.currentTimeMillis()
        ),
        FileData(
            id = 10,
            fileName = "/tmp/b.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/b.txt.token",
            lastModifiedTime = System.currentTimeMillis()
        ),
        FileData(
            id = 10,
            fileName = "/tmp/c.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/c.txt.token",
            lastModifiedTime = System.currentTimeMillis()
        ),
        FileData(
            id = 10,
            fileName = "/tmp/test",
            fileType = FileType.Folder.toString(),
            token = "/tmp/test.token",
            lastModifiedTime = System.currentTimeMillis()
        )
    )

    override fun upload(Param: HashMap<String, Any>, file: MultipartBody.Part): String = "10"

    override fun download(token: String): DownloadResponse = DownloadResponse(
        fileContents = ResponseBody.create("multipart/form-data".toMediaTypeOrNull(), "test"),
        fileName = "TestingFileName"
    )

    override fun loginUser(userLoginRequest: LoginRequest): LoginResponse = LoginResponse("1234")

    override fun register(userRegisterRequest: RegisterRequest): RegisterResponse = RegisterResponse("userId","aaa@konkuk.ac.kr")
}