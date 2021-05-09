package com.kangdroid.navi_arch.server

import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {
    @GET("/api/navi/root-token")
    fun getRootToken(): Call<RootTokenResponseDto>

    @GET("/api/navi/files/list/{token}")
    fun getInsideFiles(@Path("token") token: String): Call<List<FileData>>

    @Multipart
    @POST("/api/navi/files")
    fun upload(@PartMap par : HashMap<String,Any>, @Part files: MultipartBody.Part) : Call<ResponseBody>

    @GET("/api/navi/files/{token}")
    fun download(@Path("token") token : String) : Call<ResponseBody>

    @POST("/api/navi/login")
    fun loginUser( @Body userLoginRequest : LoginRequest): Call<ResponseBody>

    @POST("/api/navi/join")
    fun register( @Body userRegisterRequest : RegisterRequest) : Call<ResponseBody>

}