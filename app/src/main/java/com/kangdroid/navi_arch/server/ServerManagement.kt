package com.kangdroid.navi_arch.server

import android.os.Environment
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLDecoder
import javax.inject.Inject

class ServerManagement(
    private val httpUrl: HttpUrl,
    private val serverManagementHelper: ServerManagementHelper
) {

    private val logTag: String = this::class.java.simpleName
    private lateinit var retroFit: Retrofit
    private lateinit var api: APIInterface

    // Whether server connection is successful or not
    private var isServerEnabled: Boolean = false

    init {
        isServerEnabled = initWholeServerClient()
    }

    fun initWholeServerClient(): Boolean {
        var returnServerEnabled: Boolean = false
        runCatching {
            retroFit = initRetroFit()
            api = initApiInterface()
        }.onSuccess {
            returnServerEnabled = true
        }.onFailure {
            Log.e(logTag, "Error occurred when connecting to server: ${it.message}")
            Log.e(logTag, "Error Message: ${it.stackTraceToString()}")
            returnServerEnabled = false
        }

        return returnServerEnabled
    }

    // Might throw exceptions
    private fun initRetroFit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(httpUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun initApiInterface(): APIInterface {
        return retroFit.create(APIInterface::class.java)
    }


    fun getRootToken(): RootTokenResponseDto {
        val tokenFunction: Call<RootTokenResponseDto> = api.getRootToken()

        // Get response, and throw if exception occurred.
        val response: Response<RootTokenResponseDto> = serverManagementHelper.exchangeDataWithServer(tokenFunction)

        // Check for input response
        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        return response.body()
            ?: throw NoSuchFieldException("Response was OK, but wrong response body received.")
    }

    /**
     * getInsideFiles: Get list of files/directories based on requested token
     * Param: The target token to request
     * Returns: List of FileResponseDTO[The Response] - could be empty.
     * Returns: NULL when error occurred.
     */
    fun getInsideFiles(requestToken: String): List<FileData> {
        val insiderFunction: Call<List<FileData>> = api.getInsideFiles(requestToken)
        val response: Response<List<FileData>> = serverManagementHelper.exchangeDataWithServer(insiderFunction)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        return response.body()
            ?: throw NoSuchFieldException("Response was OK, but wrong response body received.")
    }

    fun upload(Param : HashMap<String,Any>, file: MultipartBody.Part) : String {
        val uploading: Call<ResponseBody> = api.upload(Param, file)
        val response: Response<ResponseBody> = serverManagementHelper.exchangeDataWithServer(uploading)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        val responseBody: ResponseBody = response.body()!!

        return responseBody.string()
    }

    fun download(token: String): DownloadResponse {
        val downloadingApi : Call<ResponseBody> = api.download(token)
        val response: Response<ResponseBody> = serverManagementHelper.exchangeDataWithServer(downloadingApi)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        // Get Content Name
        val header : String = response.headers().get("Content-Disposition").apply {
            // Since raw header is encoded with URL Scheme, decode it.
            URLDecoder.decode(this,"UTF-8")
        } ?: throw IllegalArgumentException("Content-Disposition is NEEDED somehow, but its missing!")

        // Get file Name from header
        val fileName : String = header.replace("attachment; filename=\"", "").let {
            it.substring(it.lastIndexOf("/")+1,it.length-1)
        }
        Log.d(logTag, "fileName : $fileName")
        Log.d(logTag, "Content : ${response.body()?.string()}")

        return DownloadResponse(fileName, response.body()!!)
    }
}