package com.kangdroid.navi_arch.server

import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {
    @GET("/api/navi/root-token")
    fun getRootToken(@HeaderMap headerMap: HashMap<String, Any?>): Call<RootTokenResponseDto>

    @GET("/api/navi/files/list/{token}")
    fun getInsideFiles(@HeaderMap headerMap: HashMap<String, Any?>, @Path("token") token: String): Call<List<FileData>>

    @Multipart
    @POST("/api/navi/files")
    fun upload(@HeaderMap headerMap: HashMap<String, Any?>, @PartMap par : HashMap<String, Any>, @Part files: MultipartBody.Part) : Call<ResponseBody>

    @GET("/api/navi/files")
    fun download(@HeaderMap headerMap: HashMap<String, Any?>, @Query("token") token : String, @Query("prevToken") prevToken: String) : Call<ResponseBody>

    @POST("/api/navi/login")
    fun loginUser( @Body userLoginRequest : LoginRequest): Call<LoginResponse>

    @POST("/api/navi/join")
    fun register( @Body userRegisterRequest : RegisterRequest) : Call<RegisterResponse>

    @POST("/api/navi/folder")
    fun makeFolder(@HeaderMap headerMap: HashMap<String, Any?>, @Body createFolderRequest: CreateFolderRequestDTO):Call<ResponseBody>

}